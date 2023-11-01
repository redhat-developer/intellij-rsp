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
import com.intellij.openapi.ui.DialogWrapper;
import com.redhat.devtools.intellij.rsp.client.IntelliJRspClientLauncher;
import com.redhat.devtools.intellij.rsp.model.IRsp;
import com.redhat.devtools.intellij.rsp.model.impl.RspCore;
import com.redhat.devtools.intellij.rsp.telemetry.TelemetryService;
import com.redhat.devtools.intellij.rsp.ui.tree.RspTreeModel;
import org.jboss.tools.rsp.api.dao.Status;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class DeleteServerAction extends AbstractTreeAction {
    private static final String ERROR_DELETING_SERVER = "Error deleting server";
    private static final String ERROR_DELETING_SERVERS = "Error deleting servers";

    @Override
    protected boolean isVisible(Object[] o) {
        return safeMultiItemClass(o, RspTreeModel.ServerStateWrapper.class);
    }

    @Override
    protected boolean isEnabled(Object[] o) {
        return safeMultiItemClass(o, RspTreeModel.ServerStateWrapper.class);
    }


    private static class AreYouSureDialog extends DialogWrapper {
        private boolean plural;
        public AreYouSureDialog(boolean plural) {
            super(true); // use current window as parent
            this.plural = plural;
            init();
            setTitle(plural ? "Delete Servers?" : "Delete Server?");
        }

        @Override
        protected JComponent createCenterPanel() {
            JPanel dialogPanel = new JPanel(new BorderLayout());
            String lString = this.plural ? "Are you sure you want to delete these servers?" : "Are you sure you want to delete this server?";
            JLabel label = new JLabel(lString);
            label.setPreferredSize(new Dimension(100, 100));
            dialogPanel.add(label, BorderLayout.CENTER);
            return dialogPanel;
        }
    }

    @Override
    protected void actionPerformed(AnActionEvent e, TreePath[] treePath, Object[] selected) {
        ArrayList<RspTreeModel.ServerStateWrapper> arr = new ArrayList<>();
        for( int i = 0; i < selected.length; i++ ) {
            if( selected[i] instanceof RspTreeModel.ServerStateWrapper) {
                arr.add((RspTreeModel.ServerStateWrapper)selected[i]);
            }
        }
        if( arr.size() > 0 ) {
            Project project = ProjectManager.getInstance().getOpenProjects()[0];

            if (new AreYouSureDialog(arr.size() > 1).showAndGet()) {
                new Thread("Deleting RSP Servers") {
                    public void run() {
                        ArrayList<Status> fails = new ArrayList<>();
                        for( int i = 0; i < arr.size(); i++ ) {
                            RspTreeModel.ServerStateWrapper sel = arr.get(i);
                            IntelliJRspClientLauncher client = RspCore.getDefault().getClient(sel.getRsp());
                            try {
                                Status stat = client.getServerProxy().deleteServer(sel.getServerState().getServer()).get();
                                String serverType = sel.getServerState().getServer().getType().getId();
                                TelemetryService.instance().sendWithType(TelemetryService.TELEMETRY_SERVER_REMOVE, serverType, stat);
                                if( !stat.isOK()) {
                                    fails.add(stat);
                                }
                            } catch (InterruptedException | ExecutionException ex) {
                                TelemetryService.instance().send(TelemetryService.TELEMETRY_SERVER_REMOVE, ex);
                                apiError(ex,  arr.size() > 1 ? ERROR_DELETING_SERVERS : ERROR_DELETING_SERVER);
                            }
                        }
                        if( fails.size() > 0 ) {
                            String result = fails.stream().map(Status::getMessage).collect(Collectors.joining(","));
                            showError(result, arr.size() > 1 ? ERROR_DELETING_SERVERS : ERROR_DELETING_SERVER);
                        }
                    }
                }.start();

            }
        }
    }

}
