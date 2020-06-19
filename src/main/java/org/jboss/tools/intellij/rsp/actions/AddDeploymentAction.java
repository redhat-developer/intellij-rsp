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
package org.jboss.tools.intellij.rsp.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.DialogWrapper;
import org.jboss.tools.intellij.rsp.model.IRsp;
import org.jboss.tools.intellij.rsp.model.impl.RspCore;
import org.jboss.tools.intellij.rsp.ui.dialogs.AddDeploymentDialog;
import org.jboss.tools.intellij.rsp.ui.tree.RspTreeModel;
import org.jboss.tools.intellij.rsp.ui.util.UIHelper;
import org.jboss.tools.rsp.api.RSPServer;
import org.jboss.tools.rsp.api.dao.*;

import javax.swing.tree.TreePath;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class AddDeploymentAction extends AbstractTreeAction {
    private static final String ERROR_LISTING = "Error listing deployment options";
    private static final String ERROR_ADDING = "Error adding deployment";

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
        if( selected instanceof RspTreeModel.ServerStateWrapper) {
            RspTreeModel.ServerStateWrapper wrap = (RspTreeModel.ServerStateWrapper)selected;
            IRsp rsp = wrap.getRsp();
            ServerHandle sh = wrap.getServerState().getServer();
            RSPServer rspServer = RspCore.getDefault().getClient(rsp).getServerProxy();
            new Thread("Adding Deployment") {
                public void run() {
                    actionPerformedInternal(rspServer, sh);
                }
            }.start();
        }
    }

    protected void actionPerformedInternal(RSPServer rspServer, ServerHandle sh) {
        ListDeploymentOptionsResponse options;
        try {
            options = rspServer.listDeploymentOptions(sh).get();
        } catch (InterruptedException | ExecutionException interruptedException) {
            apiError(interruptedException, ERROR_LISTING);
            return;
        }

        if( options == null || !options.getStatus().isOK()) {
            statusError(options == null ? null : options.getStatus(), ERROR_LISTING);
            return;
        }

        final Attributes attr = options.getAttributes();
        Map<String, Object> opts = new HashMap<>();
        UIHelper.executeInUI(() -> {
            Attributes attr2 = attr;
            if( attr2 == null || attr2.getAttributes() == null){
                attr2 = new Attributes();
            }
            AddDeploymentDialog dialog = new AddDeploymentDialog(attr2, opts);
            dialog.show();
            String label = dialog.getLabel();
            String path = dialog.getPath();
            if( dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
                new Thread("Adding Deployment") {
                    public void run() {
                        try {
                            Status stat = rspServer.addDeployable(asReference(sh, label, path, opts)).get();
                            if( !stat.isOK()) {
                                statusError(stat, ERROR_ADDING);
                            }
                        } catch (InterruptedException e) {
                            apiError(e, ERROR_ADDING);
                        } catch (ExecutionException e) {
                            apiError(e, ERROR_ADDING);
                        }
                    }
                }.start();
            }
        });
    }
    private ServerDeployableReference asReference(ServerHandle sh, String label, String path, Map<String, Object> options) {
        DeployableReference ref = new DeployableReference(label, path);
        ref.setOptions(options);
        ServerDeployableReference sdr = new ServerDeployableReference(sh, ref);
        return sdr;
    }
}
