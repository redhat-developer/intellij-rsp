/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.rsp;

import com.intellij.ide.util.treeView.NodeRenderer;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.tree.AsyncTreeModel;
import com.intellij.ui.tree.StructureTreeModel;
import com.intellij.ui.treeStructure.Tree;
import org.jboss.tools.intellij.rsp.model.impl.RspCore;
import org.jboss.tools.intellij.rsp.ui.tree.RspTreeModel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;


public class WindowToolFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        RspCore core = RspCore.getDefault();
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        StructureTreeModel stm = new StructureTreeModel(new RspTreeModel(core));
        Tree tree = new Tree(new AsyncTreeModel(stm));
        core.addChangeListener((Object o) -> {
            invalidatePath(o, tree);
        });

        tree.setCellRenderer(new NodeRenderer());
        PopupHandler.installPopupHandler(tree,
                "org.jboss.tools.intellij.rsp.tree", ActionPlaces.UNKNOWN);
        JScrollPane panel = ScrollPaneFactory.createScrollPane(tree);
        toolWindow.getContentManager().addContent(contentFactory.createContent(panel, "", false));
    }

    private void invalidatePath(Object o, Tree tree) {
        tree.invalidate();
    }
}
