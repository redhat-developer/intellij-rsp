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
        Status stat = null;
        Exception ex = null;
        try {
            stat = rspServer.removeDeployable(sdr).get();
        } catch (InterruptedException interruptedException) {
            ex = interruptedException;
        } catch (ExecutionException executionException) {
            ex = executionException;
        }
        if( stat == null || !stat.isOK()) {
            // TODO error
        }
    }

    public static ServerDeployableReference getServerDeployableReference(RspTreeModel.DeployableStateWrapper wrap) {
        DeployableState ds = wrap.getDeployableState();
        ServerHandle sh = wrap.getServerState().getServerState().getServer();
        ServerDeployableReference sdr = new ServerDeployableReference(sh, ds.getReference());
        return sdr;
    }
}
