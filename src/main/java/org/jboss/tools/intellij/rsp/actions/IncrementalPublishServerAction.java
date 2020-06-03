package org.jboss.tools.intellij.rsp.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.jboss.tools.intellij.rsp.client.IntelliJRspClientLauncher;
import org.jboss.tools.intellij.rsp.model.IRsp;
import org.jboss.tools.intellij.rsp.model.IRspCore;
import org.jboss.tools.intellij.rsp.model.impl.RspCore;
import org.jboss.tools.intellij.rsp.ui.tree.RspTreeModel;
import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.PublishServerRequest;
import org.jboss.tools.rsp.api.dao.Status;

import javax.swing.tree.TreePath;
import java.util.concurrent.ExecutionException;

public class IncrementalPublishServerAction extends AbstractTreeAction {
    @Override
    protected boolean isEnabled(Object o) {
        return o instanceof RspTreeModel.ServerStateWrapper;
    }

    @Override
    protected void actionPerformed(AnActionEvent e, TreePath treePath, Object selected) {
        actionPerformedInternal(e, treePath, selected, ServerManagementAPIConstants.PUBLISH_INCREMENTAL);
    }
    protected void actionPerformedInternal(AnActionEvent e, TreePath treePath, Object selected, int kind) {
        if (selected instanceof RspTreeModel.ServerStateWrapper) {
            RspTreeModel.ServerStateWrapper server = (RspTreeModel.ServerStateWrapper) selected;
            PublishServerRequest req = new PublishServerRequest();
            req.setServer(server.getServerState().getServer());
            req.setKind(kind);
            Project project = ProjectManager.getInstance().getOpenProjects()[0];
            IntelliJRspClientLauncher client = RspCore.getDefault().getClient(server.getRsp());
            try {
                Status stat = client.getServerProxy().publishAsync(req).get();
                if( !stat.isOK()) {
                    showError(stat);
                }
            } catch (InterruptedException ex) {
                showError(ex);
            } catch (ExecutionException ex) {
                showError(ex);
            }
        }
    }

    private void showError(Exception ex) {
    }

    private void showError(Status stat) {
    }
}