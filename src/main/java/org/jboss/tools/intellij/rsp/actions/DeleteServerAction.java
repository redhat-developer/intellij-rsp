package org.jboss.tools.intellij.rsp.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jboss.tools.intellij.rsp.client.IntelliJRspClientLauncher;
import org.jboss.tools.intellij.rsp.model.impl.RspCore;
import org.jboss.tools.intellij.rsp.ui.tree.RspTreeModel;
import org.jboss.tools.rsp.api.dao.Status;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.TreePath;
import java.util.concurrent.ExecutionException;

public class DeleteServerAction extends AbstractTreeAction {
    @Override
    protected void actionPerformed(AnActionEvent e, TreePath treePath, Object selected) {
        if( selected instanceof RspTreeModel.ServerStateWrapper) {
            RspTreeModel.ServerStateWrapper sel = (RspTreeModel.ServerStateWrapper)selected;
            Project project = ProjectManager.getInstance().getOpenProjects()[0];
            IntelliJRspClientLauncher client = RspCore.getDefault().getClient(sel.getRsp());
            try {
                Status stat = client.getServerProxy().deleteServer(sel.getServerState().getServer()).get();
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

    private void showError(Status stat) {
    }
    private void showError(Exception stat) {
    }
}
