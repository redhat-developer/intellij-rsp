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

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.TreePath;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class AddDeploymentAction extends AbstractTreeAction {
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
        try {
            ListDeploymentOptionsResponse options = rspServer.listDeploymentOptions(sh).get();
            if( options == null || !options.getStatus().isOK()) {
                error(null);
            } else {
                Attributes attr = options.getAttributes();
                Map<String, Object> opts = new HashMap<>();
                if( attr != null && attr.getAttributes().size() != 0 ) {
                    UIHelper.executeInUI(() -> {
                        AddDeploymentDialog dialog = new AddDeploymentDialog(attr, opts);
                        dialog.show();
                        String label = dialog.getLabel();
                        String path = dialog.getPath();
                        if( dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
                            new Thread("Adding Deployment") {
                                public void run() {
                                    try {
                                        Status stat = rspServer.addDeployable(asReference(sh, label, path, opts)).get();
                                    } catch (InterruptedException e) {
                                        error(e);
                                    } catch (ExecutionException e) {
                                        error(e);
                                    }
                                }
                            }.start();
                        }
                    });
                }
            }
        } catch (InterruptedException interruptedException) {
            error(interruptedException);
        } catch (ExecutionException executionException) {
            error(executionException);
        }
    }

    private ServerDeployableReference asReference(ServerHandle sh, String label, String path, Map<String, Object> options) {
        DeployableReference ref = new DeployableReference(label, path);
        ref.setOptions(options);
        ServerDeployableReference sdr = new ServerDeployableReference(sh, ref);
        return sdr;
    }

    private void error(Exception executionException) {
    }
}
