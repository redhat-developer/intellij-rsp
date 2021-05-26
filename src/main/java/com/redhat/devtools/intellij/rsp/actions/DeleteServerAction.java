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
import com.redhat.devtools.intellij.rsp.model.impl.RspCore;
import com.redhat.devtools.intellij.rsp.ui.tree.RspTreeModel;
import org.jboss.tools.rsp.api.dao.Status;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.concurrent.ExecutionException;

public class DeleteServerAction extends AbstractTreeAction {
    private static final String ERROR_DELETING_SERVER = "Error deleting server";

    @Override
    protected boolean isVisible(Object o) {
        return o instanceof RspTreeModel.ServerStateWrapper;
    }

    @Override
    protected boolean isEnabled(Object o) {
        return o instanceof RspTreeModel.ServerStateWrapper;
    }


    private static class AreYouSureDialog extends DialogWrapper {
        public AreYouSureDialog() {
            super(true); // use current window as parent
            init();
            setTitle("Delete Server?");
        }

        @Nullable
        @Override
        protected JComponent createCenterPanel() {
            JPanel dialogPanel = new JPanel(new BorderLayout());
            JLabel label = new JLabel("Are you sure you want to delete this server?");
            label.setPreferredSize(new Dimension(100, 100));
            dialogPanel.add(label, BorderLayout.CENTER);
            return dialogPanel;
        }
    }

    @Override
    protected void actionPerformed(AnActionEvent e, TreePath treePath, Object selected) {
        if( selected instanceof RspTreeModel.ServerStateWrapper) {
            RspTreeModel.ServerStateWrapper sel = (RspTreeModel.ServerStateWrapper)selected;
            Project project = ProjectManager.getInstance().getOpenProjects()[0];

            if (new AreYouSureDialog().showAndGet()) {
                IntelliJRspClientLauncher client = RspCore.getDefault().getClient(sel.getRsp());
                try {
                    Status stat = client.getServerProxy().deleteServer(sel.getServerState().getServer()).get();
                    if( !stat.isOK()) {
                        statusError(stat, ERROR_DELETING_SERVER);
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    apiError(ex, ERROR_DELETING_SERVER);
                }
            }
        }
    }

}
