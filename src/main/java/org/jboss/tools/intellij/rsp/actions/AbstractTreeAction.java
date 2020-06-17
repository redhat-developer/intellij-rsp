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

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.treeStructure.Tree;
import org.jboss.tools.intellij.rsp.ui.tree.RspTreeModel;
import org.jboss.tools.intellij.rsp.ui.util.UIHelper;
import org.jboss.tools.rsp.api.dao.Status;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.util.Optional;

public abstract class AbstractTreeAction extends AnAction {
    private static final String INVALID_RESPONSE = "Invalid Response from RSP";

    protected void showError(String msg, String title) {
        UIHelper.executeInUI(() -> Messages.showErrorDialog(msg, title));
    }

    protected void apiError(Exception exception, String title) {
        showError(exception == null ? "Unknown Error" : exception.getMessage(), title);
    }

    protected void statusError(Status stat, String title) {
        showError(stat == null ? INVALID_RESPONSE : stat.getMessage(), title);
    }

    @Override
    public void update(AnActionEvent e) {
        Object o = getSelectedElementFromEvent(e);
        e.getPresentation().setVisible(isVisible(o));
        e.getPresentation().setEnabled(isEnabled(o));

    }

    protected boolean isEnabled(Object o) {
        return true;
    }

    protected boolean isVisible(Object o) {
        return true;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Object userObj = getSelectedElementFromEvent(e);
        actionPerformed(e, getSelectedPath(getTree(e)).get(), userObj);
    }

    private Object getSelectedElementFromEvent(AnActionEvent e) {
        Optional<TreePath> selectedPath = getSelectedPath(getTree(e));
        if (!selectedPath.isPresent())
            return null;
        Object selected = selectedPath.get().getLastPathComponent();
        if (!(selected instanceof DefaultMutableTreeNode)) {
            return null;
        }
        DefaultMutableTreeNode selected2 = (DefaultMutableTreeNode)selected;
        Object sel = selected2.getUserObject();
        if( !(sel instanceof RspTreeModel.Descriptor)) {
            return sel;
        } else {
            return ((RspTreeModel.Descriptor)sel).getElement();
        }
    }

    protected abstract void actionPerformed(AnActionEvent e, TreePath treePath, Object selected);

    public Optional<TreePath> getSelectedPath(Tree tree) {
        return Optional.ofNullable(tree.getSelectionModel().getSelectionPath());
    }

    protected Tree getTree(AnActionEvent e) {
        return (Tree) e.getData(PlatformDataKeys.CONTEXT_COMPONENT);
    }

    @Override
    public boolean isDumbAware() {
        return false;
    }
}
