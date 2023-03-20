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
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.redhat.devtools.intellij.rsp.client.IntelliJRspClientLauncher;
import com.redhat.devtools.intellij.rsp.model.IRspCore;
import com.redhat.devtools.intellij.rsp.model.impl.RspCore;
import com.redhat.devtools.intellij.rsp.telemetry.TelemetryService;
import com.redhat.devtools.intellij.rsp.ui.dialogs.SelectDownloadRuntimeDialog;
import com.redhat.devtools.intellij.rsp.ui.util.WorkflowUiUtility;
import com.redhat.devtools.intellij.rsp.model.IRsp;
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
    protected boolean isVisible(Object[] o) {
        return safeSingleItemClass(o, IRsp.class);
    }

    @Override
    protected boolean isEnabled(Object[] o) {
        return safeSingleItemClass(o, IRsp.class) && ((IRsp) o[0]).getState() == IRspCore.IJServerState.STARTED;
    }

    @Override
    protected void singleSelectionActionPerformed(AnActionEvent e, TreePath treePath, Object selected) {
        if (selected instanceof IRsp) {
            final IRsp server = (IRsp) selected;
            if (server.getState() == IRspCore.IJServerState.STARTED) {
                Project project = ProjectManager.getInstance().getOpenProjects()[0];

                final IntelliJRspClientLauncher client = RspCore.getDefault().getClient(server);
                final SelectDownloadRuntimeDialog td = new SelectDownloadRuntimeDialog(server);
                ApplicationManager.getApplication().invokeLater(() -> {
                    td.show();
                    DownloadRuntimeDescription chosen = td.getSelected();
                    if (chosen != null && td.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
                        new Thread("Download Runtime Workflow: " + server.getRspType().getName()) {
                            public void run() {
                                TelemetryService.instance().sendWithType(TelemetryService.TELEMETRY_DOWNLOAD_RUNTIME, chosen.getId());
                                initiateDownloadRuntimeWorkflow(server, client, chosen);
                            }
                        }.start();
                    }
                });
                new Thread("Load downloadable runtimes...") {
                    public void run() {
                        ListDownloadRuntimeResponse runtimeResponse = null;
                        try {
                            runtimeResponse = client.getServerProxy().listDownloadableRuntimes().get();
                            td.setDownloadRuntimes(runtimeResponse);
                        } catch (InterruptedException | ExecutionException ex) {
                            apiError(ex, ERROR_FETCHING_DOWNLOADABLE_RUNTIMES);
                            return;
                        }

                    }
                }.start();
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