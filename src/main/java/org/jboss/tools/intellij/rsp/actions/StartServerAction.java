/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.rsp.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.jboss.tools.intellij.rsp.client.IntelliJRspClientLauncher;
import org.jboss.tools.intellij.rsp.model.impl.RspCore;
import org.jboss.tools.intellij.rsp.ui.tree.RspTreeModel;
import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.LaunchParameters;
import org.jboss.tools.rsp.api.dao.ServerAttributes;
import org.jboss.tools.rsp.api.dao.StartServerResponse;

import javax.swing.tree.TreePath;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class StartServerAction extends AbstractTreeAction {
    private static final String ERROR_STARTING_SERVER = "Error starting server";
    @Override
    protected boolean isEnabled(Object o) {
        if( o instanceof RspTreeModel.ServerStateWrapper) {
            int state = ((RspTreeModel.ServerStateWrapper)o).getServerState().getState();
            return state == ServerManagementAPIConstants.STATE_STOPPED || state == ServerManagementAPIConstants.STATE_UNKNOWN;
        }
        return false;
    }

    @Override
    protected void actionPerformed(AnActionEvent e, TreePath treePath, Object selected) {
        if (selected instanceof RspTreeModel.ServerStateWrapper) {
            final RspTreeModel.ServerStateWrapper sel = (RspTreeModel.ServerStateWrapper) selected;
            final Project project = ProjectManager.getInstance().getOpenProjects()[0];
            final IntelliJRspClientLauncher client = RspCore.getDefault().getClient(sel.getRsp());
            new Thread("Starting server: " + sel.getServerState().getServer().getId()) {
                public void run() {
                    actionPerformedInternal(sel, project, client);
                }
            }.start();
        }
    }
    protected void actionPerformedInternal(RspTreeModel.ServerStateWrapper sel, Project project, IntelliJRspClientLauncher client) {
        String mode = "run";
        ServerAttributes sa = new ServerAttributes(sel.getServerState().getServer().getType().getId(),
                sel.getServerState().getServer().getId(), new HashMap<String,Object>());
        LaunchParameters params = new LaunchParameters(sa, mode);

        try {
            StartServerResponse stat = client.getServerProxy().startServerAsync(params).get();
            if( !stat.getStatus().isOK()) {
                statusError(stat.getStatus(), ERROR_STARTING_SERVER);
            }
        } catch (InterruptedException | ExecutionException ex) {
            apiError(ex, ERROR_STARTING_SERVER);
        }
    }
}
