package org.jboss.tools.intellij.rsp.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.jboss.tools.intellij.rsp.client.IntelliJRspClientLauncher;
import org.jboss.tools.intellij.rsp.model.impl.RspCore;
import org.jboss.tools.intellij.rsp.ui.tree.RspTreeModel;
import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.*;

import javax.swing.tree.TreePath;
import java.io.*;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class StartServerAction extends AbstractTreeAction {
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
                showError(stat.getStatus());
            } else {
                // Create a dummy process that can be shown in a terminal
            }
        } catch (InterruptedException ex) {
            showError(ex);
        } catch (ExecutionException ex) {
            showError(ex);
        }
    }

    private void showError(Status stat) {
        // TODO
    }
    private void showError(Exception stat) {
        // TODO
    }



}
