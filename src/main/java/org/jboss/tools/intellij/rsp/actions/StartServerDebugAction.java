package org.jboss.tools.intellij.rsp.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.jboss.tools.intellij.rsp.client.IntelliJRspClientLauncher;
import org.jboss.tools.intellij.rsp.model.IRsp;
import org.jboss.tools.intellij.rsp.model.impl.RspCore;
import org.jboss.tools.intellij.rsp.ui.tree.RspTreeModel;
import org.jboss.tools.rsp.api.dao.LaunchParameters;
import org.jboss.tools.rsp.api.dao.ServerAttributes;
import org.jboss.tools.rsp.api.dao.StartServerResponse;
import org.jboss.tools.rsp.api.dao.Status;

import javax.swing.tree.TreePath;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class StartServerDebugAction extends AbstractTreeAction {
    @Override
    protected boolean isEnabled(Object o) {
        return o instanceof RspTreeModel.ServerStateWrapper;
    }

    @Override
    protected void actionPerformed(AnActionEvent e, TreePath treePath, Object selected) {
        if( selected instanceof RspTreeModel.ServerStateWrapper) {
            RspTreeModel.ServerStateWrapper sel = (RspTreeModel.ServerStateWrapper)selected;
            Project project = ProjectManager.getInstance().getOpenProjects()[0];
            IntelliJRspClientLauncher client = RspCore.getDefault().getClient(sel.getRsp());

            String mode = "debug";
            ServerAttributes sa = new ServerAttributes(sel.getServerState().getServer().getType().getId(),
                    sel.getServerState().getServer().getId(), new HashMap<String,Object>());
            LaunchParameters params = new LaunchParameters(sa, mode);

            try {
                StartServerResponse stat = client.getServerProxy().startServerAsync(params).get();
                if( !stat.getStatus().isOK()) {
                    showError(stat.getStatus());
                } else {
                    connectDebugger(stat);
                }
            } catch (InterruptedException ex) {
                showError(ex);
            } catch (ExecutionException ex) {
                showError(ex);
            }
        }
    }

    private void connectDebugger(StartServerResponse stat) {
        // TODO
    }

    private void showError(Status stat) {
        // TODO
    }
    private void showError(Exception stat) {
        // TODO
    }

}
