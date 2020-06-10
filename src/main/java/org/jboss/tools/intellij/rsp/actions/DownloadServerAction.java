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
import org.jboss.tools.intellij.rsp.model.IRsp;
import org.jboss.tools.intellij.rsp.model.IRspCore;
import org.jboss.tools.intellij.rsp.model.impl.RspCore;
import org.jboss.tools.intellij.rsp.ui.dialogs.SelectDownloadRuntimeDialog;
import org.jboss.tools.intellij.rsp.ui.util.UIHelper;
import org.jboss.tools.intellij.rsp.ui.util.WorkflowUiUtility;
import org.jboss.tools.rsp.api.dao.DownloadRuntimeDescription;
import org.jboss.tools.rsp.api.dao.DownloadSingleRuntimeRequest;
import org.jboss.tools.rsp.api.dao.ListDownloadRuntimeResponse;
import org.jboss.tools.rsp.api.dao.WorkflowResponse;

import javax.swing.tree.TreePath;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class DownloadServerAction extends AbstractTreeAction {
    private static final String ERROR_FETCHING_DOWNLOADABLE_RUNTIMES = "Error loading list of downloadable runtimes";
    private static final String ERROR_DOWNLOADING_RUNTIME = "Error downloading runtime";

    @Override
    protected boolean isEnabled(Object o) {
        return o instanceof IRsp && ((IRsp) o).getState() == IRspCore.IJServerState.STARTED;
    }

    @Override
    protected void actionPerformed(AnActionEvent e, TreePath treePath, Object selected) {
        if (selected instanceof IRsp) {
            final IRsp server = (IRsp) selected;
            if (server.getState() == IRspCore.IJServerState.STARTED) {
                Project project = ProjectManager.getInstance().getOpenProjects()[0];
                IntelliJRspClientLauncher client = RspCore.getDefault().getClient(server);
                ListDownloadRuntimeResponse runtimeResponse = null;
                try {
                    runtimeResponse = client.getServerProxy().listDownloadableRuntimes().get();
                } catch (InterruptedException | ExecutionException ex) {
                    apiError(ex, ERROR_FETCHING_DOWNLOADABLE_RUNTIMES);
                    return;
                }

                SelectDownloadRuntimeDialog td = new SelectDownloadRuntimeDialog(server, runtimeResponse);
                UIHelper.executeInUI(() -> {
                    td.show();
                    DownloadRuntimeDescription chosen = td.getSelected();
                    if (chosen != null && td.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
                        new Thread("Download Runtime Workflow: " + server.getRspType().getName()) {
                            public void run() {
                                initiateDownloadRuntimeWorkflow(server, client, chosen);
                            }
                        }.start();
                    }
                });
            }
        }
    }

    private void initiateDownloadRuntimeWorkflow(IRsp server, IntelliJRspClientLauncher client, DownloadRuntimeDescription chosen) {
        DownloadSingleRuntimeRequest req = new DownloadSingleRuntimeRequest();
        req.setDownloadRuntimeId(chosen.getId());
        WorkflowResponse resp = null;
        boolean done = false;
        do {
            try {
                resp = client.getServerProxy().downloadRuntime(req).get();
            } catch (InterruptedException | ExecutionException ex) {
                apiError(ex, ERROR_DOWNLOADING_RUNTIME);
            }
            boolean isComplete = WorkflowUiUtility.workflowComplete(resp);
            if (isComplete)
                return;
            Map<String, Object> toSend = WorkflowUiUtility.displayPromptsSeekWorkflowInput(resp);
            if (toSend == null) {
                return; // Give up. User canceled.
            }
            DownloadSingleRuntimeRequest req2 = new DownloadSingleRuntimeRequest();
            req2.setRequestId(resp.getRequestId());
            req2.setDownloadRuntimeId(chosen.getId());
            req2.setData(toSend);
            req = req2;
        } while (!done);
    }
}