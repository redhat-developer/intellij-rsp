package org.jboss.tools.intellij.rsp.actions;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.DialogWrapper;
import org.jboss.tools.intellij.rsp.client.IntelliJRspClientLauncher;
import org.jboss.tools.intellij.rsp.model.IRsp;
import org.jboss.tools.intellij.rsp.model.IRspCore;
import org.jboss.tools.intellij.rsp.model.impl.RspCore;
import org.jboss.tools.intellij.rsp.ui.dialogs.SelectDownloadRuntimeDialog;
import org.jboss.tools.intellij.rsp.ui.dialogs.SelectServerActionDialog;
import org.jboss.tools.intellij.rsp.ui.dialogs.WorkflowDialog;
import org.jboss.tools.intellij.rsp.ui.tree.RspTreeModel;
import org.jboss.tools.intellij.rsp.ui.util.EditorUtil;
import org.jboss.tools.intellij.rsp.ui.util.UIHelper;
import org.jboss.tools.intellij.rsp.ui.util.WorkflowUiUtility;
import org.jboss.tools.intellij.rsp.util.CommandLineUtils;
import org.jboss.tools.intellij.rsp.util.ExecUtilClone;
import org.jboss.tools.rsp.api.dao.*;

import javax.swing.tree.TreePath;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ServerActionAction extends AbstractTreeAction {
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
                try {
                    ListServerActionResponse actionResponse = client.getServerProxy().listServerActions(state.getServerState().getServer()).get();
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
                } catch (InterruptedException interruptedException) {
                    // TODO
                } catch (ExecutionException executionException) {
                    // TODO
                }
            }
        }
    }

    private void initiateActionWorkflow(RspTreeModel.ServerStateWrapper state, IntelliJRspClientLauncher client , ServerActionWorkflow chosen) {
        try {
            WorkflowResponse resp = chosen.getActionWorkflow();
            boolean done = false;
            while( !done ) {
                Map<String, Object> toSend = WorkflowUiUtility.displayPromptsSeekWorkflowInput(resp);
                if( toSend == null ) {
                    return; // Give up. User canceled.
                }
                boolean isComplete = WorkflowUiUtility.workflowComplete(resp);
                if( isComplete )
                    return;
                ServerActionRequest req = new ServerActionRequest();
                req.setActionId(chosen.getActionId());
                req.setServerId(state.getServerState().getServer().getId());
                req.setData(toSend);
                resp = client.getServerProxy().executeServerAction(req).get();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }
}
