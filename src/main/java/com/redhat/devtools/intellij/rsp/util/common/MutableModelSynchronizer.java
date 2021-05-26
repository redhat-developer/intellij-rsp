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
package com.redhat.devtools.intellij.rsp.util.common;

import com.intellij.ide.util.treeView.AbstractTreeStructure;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.ui.tree.StructureTreeModel;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.util.Enumeration;
import java.util.function.Supplier;

/**
 * Tool copied from redhat-developer/intellij-common to update the view
 */
public class MutableModelSynchronizer {
    private final StructureTreeModel treeModel;
    private final AbstractTreeStructure structure;

    public MutableModelSynchronizer(StructureTreeModel treeModel,
                                    AbstractTreeStructure structure) {
        this.treeModel = treeModel;
        this.structure = structure;
    }

    private void invalidatePath(Supplier<TreePath> pathSupplier) {
        treeModel.getInvoker().runOrInvokeLater(() -> {
            TreePath path = pathSupplier.get();
            if (path == null || path.getLastPathComponent() == treeModel.getRoot()) {
                invalidateRoot();
            } else {
                treeModel.invalidate(path, true);
            }
        });
    }

    private void invalidateRoot() {
        treeModel.invalidate();
    }

    private Object getParentElement(Object element) {
        return structure.getParentElement(element);
    }

    private TreePath getTreePath(Object element) {
        TreePath path;
        if (isRootNode(element)) {
            path = new TreePath(treeModel.getRoot());
        } else {
            path = findTreePath(element, (DefaultMutableTreeNode)treeModel.getRoot());
        }
        return path!=null?path:new TreePath(treeModel.getRoot());
    }

    private boolean isRootNode(Object element) {
        NodeDescriptor descriptor = (NodeDescriptor) ((DefaultMutableTreeNode)treeModel.getRoot()).getUserObject();
        return descriptor != null && descriptor.getElement() == element;
    }

    private TreePath findTreePath(Object element, DefaultMutableTreeNode start) {
        if (element == null
                || start == null) {
            return null;
        }
        Enumeration children = start.children();
        while (children.hasMoreElements()) {
            Object child = children.nextElement();
            if (!(child instanceof DefaultMutableTreeNode)) {
                continue;
            }
            if (hasElement(element, (DefaultMutableTreeNode) child)) {
                return new TreePath(((DefaultMutableTreeNode)child).getPath());
            }
            TreePath path = findTreePath(element, (DefaultMutableTreeNode) child);
            if (path != null) {
                return path;
            }
        }
        return null;
    }

    private boolean hasElement(Object element, DefaultMutableTreeNode node) {
        NodeDescriptor descriptor = (NodeDescriptor) node.getUserObject();
        return descriptor != null && descriptor.getElement() == element;
    }

    // My additions below
    public void refresh() { invalidatePath(() -> null);}
    public void refresh(Object element) { invalidatePath(() -> getTreePath(getParentElement(element)));}
}
