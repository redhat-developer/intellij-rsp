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
package com.redhat.devtools.intellij.rsp.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.treeStructure.Tree;
import com.redhat.devtools.intellij.rsp.ui.tree.RspTreeModel;
import com.redhat.devtools.intellij.rsp.ui.util.UIHelper;
import org.jboss.tools.rsp.api.dao.Status;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Optional;

public abstract class AbstractTreeAction extends AnAction {
    private static final String INVALID_RESPONSE = "Invalid Response from RSP";

    public static void showError(String msg, String title) {
        UIHelper.executeInUI(() -> Messages.showErrorDialog(msg, title));
    }

    public static void apiError(Exception exception, String title) {
        showError(exception == null ? "Unknown Error" : exception.getMessage(), title);
    }

    public static void statusError(Status stat, String title) {
        showError(stat == null ? INVALID_RESPONSE : stat.getMessage(), title);
    }

    @Override
    public void update(AnActionEvent e) {
        Object[] o = getSelectedElementsFromEvent(e);
        e.getPresentation().setVisible(isVisible(o));
        e.getPresentation().setEnabled(isEnabled(o));

    }

    protected boolean isEnabled(Object[] o) {
        return true;
    }

    protected boolean isVisible(Object[] o) {
        return true;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Object[] userObjs = getSelectedElementsFromEvent(e);
        actionPerformed(e, getSelectedPaths(getTree(e)), userObjs);
    }


    private Object[] getSelectedElementsFromEvent(AnActionEvent e) {
        TreePath[] selectedPaths = getSelectedPaths(getTree(e));
        if (selectedPaths == null)
            return null;
        ArrayList<Object> ret = new ArrayList<>();
        for (int i = 0; i < selectedPaths.length; i++) {
            Object selected = selectedPaths[i].getLastPathComponent();
            if ((selected instanceof DefaultMutableTreeNode)) {
                DefaultMutableTreeNode selected2 = (DefaultMutableTreeNode) selected;
                Object sel = selected2.getUserObject();
                if (!(sel instanceof RspTreeModel.Descriptor)) {
                    ret.add(sel);
                } else {
                    ret.add(((RspTreeModel.Descriptor) sel).getElement());
                }
            }
        }
        return ret.toArray();
    }

    protected void actionPerformed(AnActionEvent e, TreePath[] treePaths, Object[] selected) {
        if( treePaths != null && treePaths.length > 0 && selected != null && selected.length > 0 ) {
            singleSelectionActionPerformed(e, treePaths[0], selected[0]);
        }
    }

    protected void singleSelectionActionPerformed(AnActionEvent e, TreePath treePaths, Object selected) {

    }

    public Optional<TreePath> getSelectedPath(Tree tree) {
        return Optional.ofNullable(tree.getSelectionModel().getSelectionPath());
    }

    public TreePath[] getSelectedPaths(Tree tree) {
        return tree.getSelectionModel().getSelectionPaths();
    }

    protected Tree getTree(AnActionEvent e) {
        return (Tree) e.getData(PlatformDataKeys.CONTEXT_COMPONENT);
    }

    @Override
    public boolean isDumbAware() {
        return false;
    }

    protected boolean safeSingleItemClass( Object[] arr, Class c) {
        return arr != null && arr.length == 1 && c.isInstance(arr[0]);
    }
    protected boolean safeMultiItemClass( Object[] arr, Class c) {
        if( arr != null ) {
            for( int i = 0; i < arr.length; i++ ) {
                if( !c.isInstance(arr[i])) {
                    return false;
                }
            }
        }
        return true;
    }

}
