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
package com.redhat.devtools.intellij.rsp.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.redhat.devtools.intellij.rsp.client.IntelliJRspClientLauncher;
import com.redhat.devtools.intellij.rsp.model.impl.RspCore;
import com.redhat.devtools.intellij.rsp.telemetry.TelemetryService;
import com.redhat.devtools.intellij.rsp.ui.tree.RspTreeModel;
import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.Status;
import org.jboss.tools.rsp.api.dao.StopServerAttributes;

import javax.swing.tree.TreePath;
import java.util.concurrent.ExecutionException;

public class StopServerAction extends AbstractTreeAction {
    private static final String ERROR_STOPPING_SERVER = "Error stopping server";

    @Override
    protected boolean isVisible(Object[] o) {
        return safeSingleItemClass(o, RspTreeModel.ServerStateWrapper.class);
    }

    @Override
    protected boolean isEnabled(Object[] o) {
        if( o != null && o.length > 0 && o[0] instanceof RspTreeModel.ServerStateWrapper) {
            int state = ((RspTreeModel.ServerStateWrapper)o[0]).getServerState().getState();
            return state == ServerManagementAPIConstants.STATE_STARTED;
        }
        return false;
    }

    @Override
    protected void singleSelectionActionPerformed(AnActionEvent e, TreePath treePath, Object selected) {
        if (selected instanceof RspTreeModel.ServerStateWrapper) {
            RspTreeModel.ServerStateWrapper sel = (RspTreeModel.ServerStateWrapper) selected;
            Project project = ProjectManager.getInstance().getOpenProjects()[0];
            IntelliJRspClientLauncher client = RspCore.getDefault().getClient(sel.getRsp());
            new Thread("Stop Server: " + sel.getServerState().getServer().getId()) {
                public void run() {
                    actionInternal(sel, project, client);
                }
            }.start();
        }
    }
    private void actionInternal(RspTreeModel.ServerStateWrapper sel, Project project, IntelliJRspClientLauncher client) {
        StopServerAttributes ssa = new StopServerAttributes(sel.getServerState().getServer().getId(), false);
        String serverType = sel.getServerState().getServer().getType().getId();
        try {
            Status stat = client.getServerProxy().stopServerAsync(ssa).get();
            TelemetryService.instance().sendWithType(TelemetryService.TELEMETRY_SERVER_STOP, stat, serverType,
                    new String[]{"force"}, new String[]{Boolean.toString(false)});
            if( !stat.isOK()) {
                statusError(stat, ERROR_STOPPING_SERVER);
            }
        } catch (InterruptedException | ExecutionException ex) {
            TelemetryService.instance().sendWithType(TelemetryService.TELEMETRY_SERVER_STOP, serverType, null, ex,
                    new String[]{"force"}, new String[]{Boolean.toString(false)});

            apiError(ex, ERROR_STOPPING_SERVER);
        }
    }
}
