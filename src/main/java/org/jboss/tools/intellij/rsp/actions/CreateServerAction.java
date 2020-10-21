/*******************************************************************************
 * Copyright (c) 2019-2020 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.rsp.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import org.jboss.tools.intellij.rsp.client.IntelliJRspClientLauncher;
import org.jboss.tools.intellij.rsp.model.IRsp;
import org.jboss.tools.intellij.rsp.model.IRspCore;
import org.jboss.tools.intellij.rsp.model.impl.RspCore;
import org.jboss.tools.intellij.rsp.ui.dialogs.NewServerDialog;
import org.jboss.tools.intellij.rsp.ui.util.UIHelper;
import org.jboss.tools.rsp.api.DefaultServerAttributes;
import org.jboss.tools.rsp.api.dao.*;

import javax.swing.tree.TreePath;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CreateServerAction extends AbstractTreeAction {
    private static final String ERROR_DISCOVERY = "Error discovering server in selected folder";
    private static final String ERROR_LOADING_ATTRIBUTES = "Error requesting attributes for server type";
    private static final String ERROR_CREATING_SERVER = "Error creating server";

    @Override
    protected boolean isVisible(Object o) {
        return o instanceof IRsp;
    }

    @Override
    protected boolean isEnabled(Object o) {
        return o instanceof IRsp && ((IRsp)o).getState() == IRspCore.IJServerState.STARTED;
    }

    @Override
    protected void actionPerformed(AnActionEvent e, TreePath treePath, Object selected) {
        if( selected instanceof IRsp) {
            IRsp server = (IRsp)selected;
            if( server.getState() == IRspCore.IJServerState.STARTED) {
                Project project = ProjectManager.getInstance().getOpenProjects()[0];
                final FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
                IntelliJRspClientLauncher client = RspCore.getDefault().getClient(server);
                createServerFromBean(client, descriptor, project);
            }
        }
    }

    private void createServerFromBean(IntelliJRspClientLauncher client, FileChooserDescriptor descriptor, Project project) {
        final VirtualFile[] result = FileChooser.chooseFiles(descriptor, project, null);
        VirtualFile vf1 = result == null || result.length == 0 ? null : result[0];
        if( vf1 != null && client != null ) {
            CompletableFuture<List<ServerBean>> fut = client.getServerProxy().findServerBeans(new DiscoveryPath(vf1.getPath()));
            List<ServerBean> beans = null;
            try {
                beans = fut.get();
            } catch (InterruptedException | ExecutionException e) {
                apiError(e, ERROR_DISCOVERY);
            }
            if( beans == null || beans.size() == 0 ) {
                apiError(new Exception("No server found at " + vf1.getPath()), ERROR_DISCOVERY);
            } else {
                showCreateServerFromBeanDialog(beans, client);
            }
        }
    }

    private void showCreateServerFromBeanDialog(List<ServerBean> beans, IntelliJRspClientLauncher client) {
        ServerBean bean1 = beans.get(0);
        String typeId = bean1.getServerAdapterTypeId();
        if( typeId == null || typeId.isEmpty() ) {
            UIHelper.executeInUI(() -> {
                showError("No server found in the given folder", "Invalid Selection");
            });
            return;
        }
        ServerType st = new ServerType(typeId, null, null);
        Attributes required2 = null;
        Attributes optional2 = null;
        try {
            required2 = client.getServerProxy()
                    .getRequiredAttributes(st).get();
            optional2 = client.getServerProxy()
                    .getOptionalAttributes(st).get();
        } catch(InterruptedException | ExecutionException e ) {
            apiError(new Exception("Error loading attributes for server type " + typeId), ERROR_LOADING_ATTRIBUTES);
            return;
        }

        final HashMap<String,Object> values = new HashMap<>();
        if( required2.getAttributes().containsKey(DefaultServerAttributes.SERVER_HOME_DIR)) {
            values.put(DefaultServerAttributes.SERVER_HOME_DIR, bean1.getLocation());
        } else if( required2.getAttributes().containsKey(DefaultServerAttributes.SERVER_HOME_FILE)) {
            values.put(DefaultServerAttributes.SERVER_HOME_FILE, bean1.getLocation());
        }

        NewServerDialog td = new NewServerDialog(required2, optional2, values);
        UIHelper.executeInUI(() -> {
            td.show();
            if( td.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
                if( td.getName() == null || td.getName().trim().isEmpty()) {
                    showError("Name must not be empty or missing", "Invalid Name");
                } else {
                    ServerAttributes csa = new ServerAttributes(typeId, td.getName(), values);
                    try {
                        CreateServerResponse result = client.getServerProxy().createServer(csa).get();
                        if (!result.getStatus().isOK()) {
                            statusError(result.getStatus(), ERROR_CREATING_SERVER);
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        apiError(e, ERROR_CREATING_SERVER);
                    }
                }
            }
        });
    }
}
