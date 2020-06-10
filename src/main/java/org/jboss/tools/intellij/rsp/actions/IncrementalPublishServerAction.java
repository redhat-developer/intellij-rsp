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
import org.jboss.tools.rsp.api.dao.PublishServerRequest;
import org.jboss.tools.rsp.api.dao.Status;

import javax.swing.tree.TreePath;
import java.util.concurrent.ExecutionException;

public class IncrementalPublishServerAction extends AbstractTreeAction {
    private static final String ERROR_PUBLISHING = "Error publishing to server";
    @Override
    protected boolean isEnabled(Object o) {
        return o instanceof RspTreeModel.ServerStateWrapper;
    }

    @Override
    protected void actionPerformed(AnActionEvent e, TreePath treePath, Object selected) {
        actionPerformedInternal(e, treePath, selected, ServerManagementAPIConstants.PUBLISH_INCREMENTAL);
    }
    protected void actionPerformedInternal(AnActionEvent e, TreePath treePath, Object selected, int kind) {
        if (selected instanceof RspTreeModel.ServerStateWrapper) {
            RspTreeModel.ServerStateWrapper server = (RspTreeModel.ServerStateWrapper) selected;
            PublishServerRequest req = new PublishServerRequest();
            req.setServer(server.getServerState().getServer());
            req.setKind(kind);
            Project project = ProjectManager.getInstance().getOpenProjects()[0];
            IntelliJRspClientLauncher client = RspCore.getDefault().getClient(server.getRsp());
            try {
                Status stat = client.getServerProxy().publishAsync(req).get();
                if( !stat.isOK()) {
                    statusError(stat, ERROR_PUBLISHING);
                }
            } catch (InterruptedException | ExecutionException ex) {
                apiError(ex, ERROR_PUBLISHING);
            }
        }
    }
}