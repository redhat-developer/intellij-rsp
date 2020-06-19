package org.jboss.tools.intellij.rsp.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jboss.tools.intellij.rsp.ui.tree.RspTreeModel;

import javax.swing.tree.TreePath;

public class EditServerAction extends AbstractTreeAction {
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

    }
}