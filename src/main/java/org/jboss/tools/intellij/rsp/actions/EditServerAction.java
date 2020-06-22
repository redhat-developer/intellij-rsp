package org.jboss.tools.intellij.rsp.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import org.jboss.tools.intellij.rsp.client.IntelliJRspClientLauncher;
import org.jboss.tools.intellij.rsp.editor.EditServerListener;
import org.jboss.tools.intellij.rsp.model.impl.RspCore;
import org.jboss.tools.intellij.rsp.ui.tree.RspTreeModel;
import org.jboss.tools.rsp.api.dao.GetServerJsonResponse;

import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class EditServerAction extends AbstractTreeAction {
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
        if (selected instanceof RspTreeModel.ServerStateWrapper) {
            RspTreeModel.ServerStateWrapper server = (RspTreeModel.ServerStateWrapper) selected;
            Project project = ProjectManager.getInstance().getOpenProjects()[0];
            IntelliJRspClientLauncher client = RspCore.getDefault().getClient(server.getRsp());
            try {
                GetServerJsonResponse response = client.getServerProxy().getServerAsJson(server.getServerState().getServer()).get();
                if (response.getStatus() != null && !response.getStatus().isOK()) {
                    showError(response.getStatus().getMessage(), "Error loading server descriptor content.");
                } else {
                    // OK assumed
                    VirtualFile vf = new LightVirtualFile(server.getServerState().getServer().getId(), response.getServerJson());
                    Key<String> KEY_RSP_ID = EditServerListener.KEY_RSP_ID;
                    Key<String> KEY_SERVER_ID = EditServerListener.KEY_SERVER_ID;

                    vf.putUserData(KEY_RSP_ID, server.getRsp().getRspType().getId());
                    vf.putUserData(KEY_SERVER_ID, server.getServerState().getServer().getId());

                    try {
                        vf.setWritable(true);
                        FileEditor[] editors = FileEditorManager.getInstance(project).openFile(vf, true);
                    } catch (IOException ioException) {
                        showError(ioException.getMessage(), "Error displaying server descriptor conetnt.");
                    }
                }
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            } catch (ExecutionException executionException) {
                executionException.printStackTrace();
            }
        }
    }
}