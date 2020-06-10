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
import org.jboss.tools.intellij.rsp.model.IRsp;
import org.jboss.tools.intellij.rsp.model.impl.RspCore;
import org.jboss.tools.intellij.rsp.ui.tree.RspTreeModel;
import org.jboss.tools.rsp.api.RSPServer;
import org.jboss.tools.rsp.api.dao.DeployableState;
import org.jboss.tools.rsp.api.dao.ServerDeployableReference;
import org.jboss.tools.rsp.api.dao.ServerHandle;
import org.jboss.tools.rsp.api.dao.Status;

import javax.swing.tree.TreePath;
import java.util.concurrent.ExecutionException;

public class RemoveDeploymentAction extends AbstractTreeAction {
    private static final String ERROR_REMOVING_DEPLOYMENT = "Error removing deployment";
    protected boolean isEnabled(Object o) {
        return o instanceof RspTreeModel.DeployableStateWrapper;
    }

    @Override
    protected void actionPerformed(AnActionEvent e, TreePath treePath, Object selected) {
        if( selected instanceof RspTreeModel.DeployableStateWrapper) {
            RspTreeModel.DeployableStateWrapper wrap = (RspTreeModel.DeployableStateWrapper)selected;
            ServerDeployableReference sdr = getServerDeployableReference(wrap);
            IRsp rsp = wrap.getServerState().getRsp();
            RSPServer rspServer = RspCore.getDefault().getClient(rsp).getServerProxy();
            new Thread("Remove Deployment") {
                public void run() {
                    actionPerformedThread(rspServer, sdr);
                }
            }.start();
        }
    }

    protected void actionPerformedThread(RSPServer rspServer, ServerDeployableReference sdr) {
        try {
            Status stat = rspServer.removeDeployable(sdr).get();
            if( stat == null || !stat.isOK()) {
                statusError(stat, ERROR_REMOVING_DEPLOYMENT);
            }
        } catch (InterruptedException | ExecutionException ex) {
            apiError(ex, ERROR_REMOVING_DEPLOYMENT);
        }
    }

    public static ServerDeployableReference getServerDeployableReference(RspTreeModel.DeployableStateWrapper wrap) {
        DeployableState ds = wrap.getDeployableState();
        ServerHandle sh = wrap.getServerState().getServerState().getServer();
        ServerDeployableReference sdr = new ServerDeployableReference(sh, ds.getReference());
        return sdr;
    }


}
