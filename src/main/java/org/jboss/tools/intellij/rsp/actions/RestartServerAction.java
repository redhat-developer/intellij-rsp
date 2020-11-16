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
import org.jboss.tools.intellij.rsp.model.IRsp;
import org.jboss.tools.intellij.rsp.model.IRspCoreChangeListener;
import org.jboss.tools.intellij.rsp.model.impl.RspCore;
import org.jboss.tools.intellij.rsp.ui.tree.RspTreeModel;
import org.jboss.tools.rsp.api.RSPClient;
import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.*;

import javax.swing.tree.TreePath;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class RestartServerAction extends AbstractTreeAction {
    private static final String ERROR_STOPPING_SERVER = "Error stopping server";

    @Override
    protected boolean isVisible(Object o) {
        return o instanceof RspTreeModel.ServerStateWrapper;
    }

    @Override
    protected boolean isEnabled(Object o) {
        if( o instanceof RspTreeModel.ServerStateWrapper) {
            int state = ((RspTreeModel.ServerStateWrapper)o).getServerState().getState();
            return state == ServerManagementAPIConstants.STATE_STARTED;
        }
        return false;
    }

    @Override
    protected void actionPerformed(AnActionEvent e, TreePath treePath, Object selected) {
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
        IRspCoreChangeListener listener = new IRspCoreChangeListener() {
            @Override
            public void modelChanged(Object item) {
                if( item instanceof IRsp) {
                    IRsp r = (IRsp)item;
                    ServerState state = r.getModel().findServerInRsp(r, sel.getServerState().getServer().getId());
                    if( state != null ) {
                        if( state.getState() == ServerManagementAPIConstants.STATE_STOPPED) {
                            final IRspCoreChangeListener l2 = this;
                            new Thread("Restart server") {
                                public void run() {
                                    RspCore.getDefault().removeChangeListener(l2);
                                    startServer(sel, project, client);
                                }
                            }.start();
                        }
                    }
                }
            }

        };
        RspCore.getDefault().addChangeListener(listener);
        stopServer(sel, client);
    }

    protected void startServer(RspTreeModel.ServerStateWrapper sel, Project project, IntelliJRspClientLauncher client) {
        StartServerAction.startServerRunModeInternal(sel, project, client);
    }

    private void stopServer(RspTreeModel.ServerStateWrapper sel, IntelliJRspClientLauncher client) {
        StopServerAttributes ssa = new StopServerAttributes(sel.getServerState().getServer().getId(), false);
        try {
            Status stat = client.getServerProxy().stopServerAsync(ssa).get();
            if( !stat.isOK()) {
                statusError(stat, ERROR_STOPPING_SERVER);
            }
        } catch (InterruptedException | ExecutionException ex) {
            apiError(ex, ERROR_STOPPING_SERVER);
        }
    }
}
