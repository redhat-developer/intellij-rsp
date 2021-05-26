package com.redhat.devtools.intellij.rsp.extension;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.vfs.VirtualFile;
import com.redhat.devtools.intellij.common.editor.AllowNonProjectEditing;
import org.jetbrains.annotations.NotNull;

public class RSPPluginInitializer implements StartupActivity {
    @Override
    public void runActivity(@NotNull Project project) {
        enableNonProjectEditing(project);
    }

    private void enableNonProjectEditing( Project project ) {
        FileEditor[] editors = FileEditorManager.getInstance(project).getAllEditors();
        for( int i = 0; i < editors.length; i++ ) {
            // TODO: Check if its a file I want to set the flag on or not
            VirtualFile vf = getResourceFile(editors[i]);
            enableNonProjectFileEditing(vf);
        }
    }

    private VirtualFile getResourceFile(FileEditor editor) {
        return editor.getFile();
    }

    private void enableNonProjectFileEditing(VirtualFile file) {
        if( file != null ) {
            file.putUserData(AllowNonProjectEditing.ALLOW_NON_PROJECT_EDITING, true);
        }
    }
}
