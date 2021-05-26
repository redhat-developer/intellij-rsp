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
package com.redhat.devtools.intellij.rsp.ui.util;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.redhat.devtools.intellij.common.editor.AllowNonProjectEditing;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Utilities related to opening editors
 */
public class EditorUtil {
    public static void openFileInEditor(Project project, File f) {
        VirtualFile vf =  LocalFileSystem.getInstance().refreshAndFindFileByIoFile(f);
        vf.putUserData(AllowNonProjectEditing.ALLOW_NON_PROJECT_EDITING, true);
        FileEditorManager.getInstance(project).openFile(vf, true);
    }

    public static void createAndOpenVirtualFile(String name, String content, Project project) {//, String namespace, String kind) {
        try {
            VirtualFile vf = createTempFile(name, content);
            if( vf != null ) {
                vf.putUserData(AllowNonProjectEditing.ALLOW_NON_PROJECT_EDITING, true);
                File fileToDelete = new File(vf.getPath());
                fileToDelete.deleteOnExit();
                FileEditorManager.getInstance(project).openFile(vf, true);
            }
        } catch (IOException e) {
            // TODO
        }
    }

    private static VirtualFile createTempFile(String name, String content) throws IOException {
        File file = new File(System.getProperty("java.io.tmpdir"), name);
        FileUtils.write(file, content, StandardCharsets.UTF_8);
        return LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
    }
}
