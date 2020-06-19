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
import com.intellij.openapi.ui.DialogWrapper;
import org.jboss.tools.intellij.rsp.client.IntelliJRspClientLauncher;
import org.jboss.tools.intellij.rsp.model.IRspCore;
import org.jboss.tools.intellij.rsp.model.impl.RspCore;
import org.jboss.tools.intellij.rsp.ui.dialogs.SelectServerActionDialog;
import org.jboss.tools.intellij.rsp.ui.tree.RspTreeModel;
import org.jboss.tools.intellij.rsp.ui.util.UIHelper;
import org.jboss.tools.intellij.rsp.ui.util.WorkflowUiUtility;
import org.jboss.tools.rsp.api.dao.ListServerActionResponse;
import org.jboss.tools.rsp.api.dao.ServerActionRequest;
import org.jboss.tools.rsp.api.dao.ServerActionWorkflow;
import org.jboss.tools.rsp.api.dao.WorkflowResponse;

import javax.swing.tree.TreePath;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ServerActionAction extends AbstractTreeAction {
    private static final String ERROR_LISTING_ACTIONS = "Error listing server actions";
    private static final String ERROR_EXECUTE_ACTIONS = "Error executing server action";

    @Override
    protected boolean isVisible(Object o) {
        return o instanceof RspTreeModel.ServerStateWrapper;
    }

    @Override
    protected boolean isEnabled(Object o) {
        return o instanceof RspTreeModel.ServerStateWrapper;
    }

    @Override
    protected void actionPerformed(AnActionEvent e, TreePath treePath, Object selected) {
        if( selected instanceof RspTreeModel.ServerStateWrapper) {
            RspTreeModel.ServerStateWrapper state = (RspTreeModel.ServerStateWrapper)selected;
            Project project = ProjectManager.getInstance().getOpenProjects()[0];
            IntelliJRspClientLauncher client = RspCore.getDefault().getClient(state.getRsp());
            if (state.getRsp().getState() == IRspCore.IJServerState.STARTED) {
                ListServerActionResponse actionResponse = null;
                try {
                    actionResponse = client.getServerProxy().listServerActions(state.getServerState().getServer()).get();
                } catch (InterruptedException | ExecutionException ex) {
                    apiError(ex, ERROR_LISTING_ACTIONS);
                    return;
                }

                SelectServerActionDialog td = new SelectServerActionDialog(state,actionResponse);
                UIHelper.executeInUI(() -> {
                    td.show();
                    ServerActionWorkflow chosen = td.getSelected();
                    if( chosen != null && td.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
                        new Thread("Server Action Workflow: " + chosen.getActionLabel()) {
                            public void run() {
                                initiateActionWorkflow(state, client, chosen);
                            }
                        }.start();
                    }
                });
            }
        }
    }

    private void initiateActionWorkflow(RspTreeModel.ServerStateWrapper state, IntelliJRspClientLauncher client , ServerActionWorkflow chosen) {
        WorkflowResponse resp = chosen.getActionWorkflow();
        boolean done = false;
        while (!done) {
            Map<String, Object> toSend = WorkflowUiUtility.displayPromptsSeekWorkflowInput(resp);
            if (toSend == null) {
                return; // Give up. User canceled.
            }
            boolean isComplete = WorkflowUiUtility.workflowComplete(resp);
            if (isComplete)
                return;
            ServerActionRequest req = new ServerActionRequest();
            req.setActionId(chosen.getActionId());
            req.setServerId(state.getServerState().getServer().getId());
            req.setData(toSend);
            try {
                resp = client.getServerProxy().executeServerAction(req).get();
            } catch (InterruptedException | ExecutionException e) {
                apiError(e, ERROR_EXECUTE_ACTIONS);
            }
        }
    }
}
