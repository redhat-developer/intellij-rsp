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
package com.redhat.devtools.intellij.rsp.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.redhat.devtools.intellij.rsp.telemetry.TelemetryService;
import com.redhat.devtools.intellij.rsp.ui.tree.RspTreeModel;
import com.redhat.devtools.intellij.rsp.model.IRsp;
import com.redhat.devtools.intellij.rsp.model.impl.RspCore;
import org.jboss.tools.rsp.api.RSPServer;
import org.jboss.tools.rsp.api.dao.DeployableState;
import org.jboss.tools.rsp.api.dao.ServerDeployableReference;
import org.jboss.tools.rsp.api.dao.ServerHandle;
import org.jboss.tools.rsp.api.dao.Status;

import javax.swing.tree.TreePath;
import java.util.concurrent.ExecutionException;

public class RemoveDeploymentAction extends AbstractTreeAction {
    private static final String ERROR_REMOVING_DEPLOYMENT = "Error removing deployment";

    @Override
    protected boolean isVisible(Object[] o) {
        return safeSingleItemClass(o, RspTreeModel.DeployableStateWrapper.class);
    }

    @Override
    protected boolean isEnabled(Object[] o) {
        return safeSingleItemClass(o, RspTreeModel.DeployableStateWrapper.class);
    }

    @Override
    protected void singleSelectionActionPerformed(AnActionEvent e, TreePath treePath, Object selected) {
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
            TelemetryService.instance().sendWithType(TelemetryService.TELEMETRY_DEPLOYMENT_REMOVE, sdr.getServer().getType().getId(), stat);
            if( stat == null || !stat.isOK()) {
                statusError(stat, ERROR_REMOVING_DEPLOYMENT);
            }
        } catch (InterruptedException | ExecutionException ex) {
            TelemetryService.instance().sendWithType(TelemetryService.TELEMETRY_DEPLOYMENT_REMOVE, sdr.getServer().getType().getId(), ex);
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
