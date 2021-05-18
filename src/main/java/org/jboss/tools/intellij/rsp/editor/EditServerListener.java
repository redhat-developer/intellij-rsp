package org.jboss.tools.intellij.rsp.editor;

import com.fasterxml.jackson.databind.JsonNode;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileDocumentSynchronizationVetoer;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import org.jboss.tools.intellij.rsp.client.IntelliJRspClientLauncher;
import org.jboss.tools.intellij.rsp.model.IRsp;
import org.jboss.tools.intellij.rsp.model.IRspType;
import org.jboss.tools.intellij.rsp.model.impl.RspCore;
import org.jboss.tools.intellij.rsp.ui.util.UIHelper;
import org.jboss.tools.rsp.api.dao.*;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutionException;

public class EditServerListener extends FileDocumentSynchronizationVetoer {
    public static final String NOTIFICATION_ID = "RSP Server";
    public static final String EDITOR_KEY_RSP_ID = "rsp.editor.rsp.id";
    public static final String EDITOR_KEY_SERVER_ID = "rsp.editor.server.id";
    public static final Key<String> KEY_RSP_ID = Key.create(EditServerListener.EDITOR_KEY_RSP_ID);
    public static final Key<String> KEY_SERVER_ID = Key.create(EditServerListener.EDITOR_KEY_SERVER_ID);


    @Override
    public boolean maySaveDocument(@NotNull Document document, boolean isSaveExplicit) {
        VirtualFile vf = FileDocumentManager.getInstance().getFile(document);
        String rspId = vf.getUserData(KEY_RSP_ID);
        String serverId = vf.getUserData(KEY_SERVER_ID);

        if(!isFileToPush(document, vf)) {
            return true;
        }

        IRsp rspObj = RspCore.getDefault().findRsp(rspId);
        ServerState ss = null;
        String error = null;
        if( rspObj == null ) {
            error = "RSP " + rspId + " not found.";
        } else {
            ss = RspCore.getDefault().findServerInRsp(rspObj, serverId);
            if( ss == null ) {
                error = "Server " + serverId + " not found in RSP " + rspId;
            }
        }

        if( error != null ) {
            Notification notification = new Notification(NOTIFICATION_ID, "Error",
                    "An error occurred: " + error, NotificationType.ERROR);
            Notifications.Bus.notify(notification);
            return true;
        }

        IntelliJRspClientLauncher client = rspObj.getModel().getClient(rspObj);
        UpdateServerResponse saveResult = null;
        try {
            UpdateServerRequest request = new UpdateServerRequest();
            request.setHandle(ss.getServer());
            request.setServerJson(document.getText());
            saveResult = client.getServerProxy().updateServer(request).get();
        } catch (InterruptedException | ExecutionException e) {
            Notification notification = new Notification(NOTIFICATION_ID, "Error",
                    "An error occurred while saving text to RSP: " + e.getMessage(), NotificationType.ERROR);
            Notifications.Bus.notify(notification);
            return true;
        }

        if( saveResult == null ) {
            Notification notification = new Notification(NOTIFICATION_ID, "Error",
                    "An error occurred while saving text to RSP: Null response from server", NotificationType.ERROR);
            Notifications.Bus.notify(notification);
            return true;
        }

        if( !saveResult.getValidation().getStatus().isOK()) {
            Status stat = saveResult.getValidation().getStatus();
            document.setText(saveResult.getServerJson().getServerJson());
            Notification notification = new Notification(NOTIFICATION_ID, "Error",
                    "An error occurred while saving text to RSP: " + stat.getMessage(), NotificationType.ERROR);
            Notifications.Bus.notify(notification);
            return true;
        }
        GetServerJsonResponse gsjr = saveResult.getServerJson();
        String newServerString = gsjr == null ? null : saveResult.getServerJson().getServerJson();
        // TODO document.setText(newServerString);
        // notify user if saving was completed successfully
        Notification notification = new Notification(NOTIFICATION_ID, "Save Successful",
                "Server " + ss.getServer().getId() + " saved successfully.", NotificationType.INFORMATION);
        Notifications.Bus.notify(notification);
        return true;
    }

    private boolean isFileToPush(Document document, VirtualFile vf) {
        Project project = ProjectManager.getInstance().getOpenProjects()[0];
        Editor selectedEditor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        // if file is not the one selected, skip it
        if (selectedEditor == null || selectedEditor.getDocument() != document) return false;
        // if file is not related to rsp, skip it
        if (vf == null || vf.getUserData(KEY_RSP_ID) == null || vf.getUserData(KEY_SERVER_ID) == null) {
            return false;
        }
        return true;
    }
}
