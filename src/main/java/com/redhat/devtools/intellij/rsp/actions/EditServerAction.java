package com.redhat.devtools.intellij.rsp.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.redhat.devtools.intellij.rsp.client.IntelliJRspClientLauncher;
import com.redhat.devtools.intellij.rsp.model.impl.RspCore;
import com.redhat.devtools.intellij.rsp.ui.tree.RspTreeModel;
import org.apache.commons.io.FileUtils;
import com.redhat.devtools.intellij.rsp.editor.EditServerListener;
import org.jboss.tools.rsp.api.dao.GetServerJsonResponse;

import javax.swing.tree.TreePath;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class EditServerAction extends AbstractTreeAction {
    @Override
    protected boolean isVisible(Object[] o) {
        return safeSingleItemClass(o, RspTreeModel.ServerStateWrapper.class);
    }

    @Override
    protected boolean isEnabled(Object[] o) {
        return safeSingleItemClass(o, RspTreeModel.ServerStateWrapper.class);
    }

    @Override
    protected void singleSelectionActionPerformed(AnActionEvent e, TreePath treePath, Object selected) {
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
                    String fName = server.getServerState().getServer().getId() + ".json";
                    //VirtualFile vf = new LightVirtualFile(fName, response.getServerJson());
                    VirtualFile vf = createTempFile(fName, response.getServerJson());
                    Key<String> KEY_RSP_ID = EditServerListener.KEY_RSP_ID;
                    Key<String> KEY_SERVER_ID = EditServerListener.KEY_SERVER_ID;

                    vf.putUserData(KEY_RSP_ID, server.getRsp().getRspType().getId());
                    vf.putUserData(KEY_SERVER_ID, server.getServerState().getServer().getId());
                    try {
                        vf.setWritable(true);
                        OpenFileDescriptor desc = new OpenFileDescriptor(project, vf, 0);
                        Editor editors = FileEditorManager.getInstance(project).openTextEditor(desc, true);
                    } catch (IOException ioException) {
                        showError(ioException.getMessage(), "Error displaying server descriptor content.");
                    }
                }
            } catch (InterruptedException interruptedException) {
                showError("Error displaying server descriptor content: " + interruptedException.getMessage(), "Error");
            } catch (ExecutionException executionException) {
                showError("Error displaying server descriptor content: " + executionException.getMessage(), "Error");
            } catch (IOException ioe) {
                showError("Error displaying server descriptor content: " + ioe.getMessage(), "Error");
            }
        }
    }


    private static VirtualFile createTempFile(String name, String content) throws IOException {
        File file = new File(System.getProperty("java.io.tmpdir"), name);
        if (file.exists()){
            file.delete();
            LocalFileSystem.getInstance().refreshIoFiles(Arrays.asList(file));
        }
        FileUtils.write(file, content, StandardCharsets.UTF_8);
        file.deleteOnExit();
        return LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
    }
}