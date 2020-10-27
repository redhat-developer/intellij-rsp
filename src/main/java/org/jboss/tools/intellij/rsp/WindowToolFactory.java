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

import com.intellij.ide.util.treeView.AbstractTreeStructure;
import com.intellij.ide.util.treeView.NodeRenderer;
import com.intellij.openapi.Disposable;
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
import org.jboss.tools.intellij.rsp.util.common.MutableModelSynchronizer;
import org.jetbrains.annotations.NotNull;

import javax.swing.JScrollPane;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * The primary view
 */
public class WindowToolFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        try {
            RspCore core = RspCore.getDefault();
            ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
            RspTreeModel rspTreeModel = new RspTreeModel(core);
            StructureTreeModel stm = buildModel(rspTreeModel, project);
            AsyncTreeModel asyncModel = new AsyncTreeModel(stm);
            Tree tree = new Tree(asyncModel);
            core.addChangeListener((Object o) -> {
                refresh(o, stm, rspTreeModel);
            });

            tree.setCellRenderer(new NodeRenderer());
            PopupHandler.installPopupHandler(tree,
                    "org.jboss.tools.intellij.rsp.tree", ActionPlaces.UNKNOWN);
            JScrollPane panel = ScrollPaneFactory.createScrollPane(tree);
            toolWindow.getContentManager().addContent(contentFactory.createContent(panel, "", false));
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException e) {
            throw new RuntimeException((e));
        }
    }

    private void refresh(Object o, StructureTreeModel stm, RspTreeModel rspTreeModel) {
        new MutableModelSynchronizer(stm, rspTreeModel).refresh();
    }

    /**
     * Build the model through reflection as StructureTreeModel does not have a stable API.
     *
     * @param structure the structure to associate
     * @param project the IJ project
     * @return the build model
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     * @throws NoSuchMethodException
     */
    private StructureTreeModel buildModel(RspTreeModel structure, Project project) throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        try {
            Constructor<StructureTreeModel> constructor = StructureTreeModel.class.getConstructor(new Class[] {AbstractTreeStructure.class});
            return constructor.newInstance(structure);
        } catch (NoSuchMethodException e) {
            Constructor<StructureTreeModel> constructor = StructureTreeModel.class.getConstructor(new Class[] {AbstractTreeStructure.class, Disposable.class});
            return constructor.newInstance(structure, project);
        }
    }
}
