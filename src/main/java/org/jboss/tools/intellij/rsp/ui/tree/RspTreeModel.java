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
package org.jboss.tools.intellij.rsp.ui.tree;

import com.intellij.icons.AllIcons;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeStructure;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.ide.util.treeView.PresentableNodeDescriptor;
import org.jboss.tools.intellij.rsp.model.IRspServer;
import org.jboss.tools.intellij.rsp.model.impl.RspCore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class RspTreeModel extends AbstractTreeStructure {
    private RspCore core;
    public RspTreeModel(RspCore core) {
        super();
        this.core = core;
    }

    @NotNull
    @Override
    public Object getRootElement() {
        return core;
    }

    @NotNull
    @Override
    public Object[] getChildElements(@NotNull Object element) {
        if( element == core )
            return core.getRSPs();
        if(element instanceof IRspServer) {
            return core.tmpGetChildren((IRspServer)element);
        }
        return new Object[0];
    }

    @Nullable
    @Override
    public Object getParentElement(@NotNull Object element) {
        if( element == core )
            return null;
        if( element instanceof IRspServer )
            return core;
        return null;
    }

    @NotNull
    @Override
    public PresentableNodeDescriptor createDescriptor(@NotNull Object element, @Nullable NodeDescriptor parentDescriptor) {
        if( element instanceof RspCore )
            return new CoreDescriptor((RspCore)element, parentDescriptor);
        if( element instanceof IRspServer ) {
            return new RspServerDescriptor((IRspServer)element, parentDescriptor);
        }
        return new StandardDescriptor((Object)element, parentDescriptor);
    }

    private interface InnerLabelProvider {
        public String getText();
    }

    private class CoreDescriptor extends Descriptor<RspCore> {
        protected CoreDescriptor(RspCore element, @Nullable NodeDescriptor parentDescriptor) {
            super(element, parentDescriptor, () -> "Runtime Server Protocol", AllIcons.General.BalloonError);
        }
    }

    private class RspServerDescriptor extends Descriptor<IRspServer> {
        protected RspServerDescriptor(IRspServer element, @Nullable NodeDescriptor parentDescriptor) {
            super(element, parentDescriptor, () -> element.getServerType().getName() + "   [" + element.getState() + "]", element.getServerType().getIcon());
        }
    }


    private class StandardDescriptor extends Descriptor<Object> {
        protected StandardDescriptor(Object element, @Nullable NodeDescriptor parentDescriptor) {
            super(element, parentDescriptor, () -> element.toString(), AllIcons.General.BalloonError);
        }
    }

    public static class Descriptor<E> extends PresentableNodeDescriptor<E> {
        private final E element;
        private final InnerLabelProvider labelProvider;
        private final Icon icon;

        protected Descriptor(E element, @Nullable NodeDescriptor parentDescriptor,
                             InnerLabelProvider provider, Icon icon) {
            super(null, parentDescriptor);
            this.element = element;
            this.labelProvider = provider;
            this.icon = icon;
        }

        @Override
        protected void update(@NotNull PresentationData presentation) {
            presentation.setPresentableText(labelProvider == null ? element.toString() : labelProvider.getText());
            if( icon != null )
                presentation.setIcon(icon);
        }

        @Override
        public E getElement() {
            return element;
        }
    }

    @Override
    public void commit() {
    }

    @Override
    public boolean hasSomethingToCommit() {
        return false;
    }
}
