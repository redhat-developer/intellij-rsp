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
package com.redhat.devtools.intellij.rsp.ui.tree;

import com.intellij.icons.AllIcons;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeStructure;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.ide.util.treeView.PresentableNodeDescriptor;
import com.intellij.openapi.util.IconLoader;
import com.redhat.devtools.intellij.rsp.model.IRsp;
import com.redhat.devtools.intellij.rsp.model.IRspCore;
import com.redhat.devtools.intellij.rsp.model.impl.RspCore;
import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.DeployableState;
import org.jboss.tools.rsp.api.dao.ServerState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

/**
 * The primary model of the tree / view used by users.
 * This primarily wraps the objects of the underlying Core model
 * to ensure a heirarchy that can be traced up and down, as well as
 * provides labels and icons.
 */
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
        if(element instanceof IRsp) {
            return wrap((IRsp)element, core.getServersInRsp((IRsp)element));
        }
        if( element instanceof ServerStateWrapper ) {
            List<DeployableState> ds = ((ServerStateWrapper)element).ss.getDeployableStates();
            return wrapDeployableStates((ServerStateWrapper)element, ds);
        }
        return new Object[0];
    }

    private DeployableStateWrapper[] wrapDeployableStates(ServerStateWrapper element, List<DeployableState> ds) {
        if( ds == null )
            return new DeployableStateWrapper[0];

        DeployableStateWrapper[] ret = new DeployableStateWrapper[ds.size()];
        int i = 0;
        for( DeployableState ds1 : ds) {
            ret[i++] = new DeployableStateWrapper(element, ds1);
        }
        return ret;
    }

    private ServerStateWrapper[] wrap(IRsp rsp, ServerState[] state) {
        ServerStateWrapper[] wrappers = new ServerStateWrapper[state.length];
        for( int i = 0; i < state.length; i++ ) {
            wrappers[i] = new ServerStateWrapper(rsp, state[i]);
        }
        return wrappers;
    }

    public static class ServerStateWrapper {
        private IRsp rsp;
        private ServerState ss;
        public ServerStateWrapper(IRsp rsp, ServerState ss) {
            this.rsp = rsp;
            this.ss = ss;
        }

        public IRsp getRsp() {
            return rsp;
        }

        public ServerState getServerState() {
            return ss;
        }
    }
    public static class DeployableStateWrapper {
        private ServerStateWrapper serverState;
        private DeployableState ds;
        public DeployableStateWrapper(ServerStateWrapper serverState, DeployableState ds) {
            this.serverState = serverState;
            this.ds = ds;
        }

        public ServerStateWrapper getServerState() {
            return serverState;
        }
        public DeployableState getDeployableState() {
            return ds;
        }
    }

    @Nullable
    @Override
    public Object getParentElement(@NotNull Object element) {
        if( element == core )
            return null;
        if( element instanceof IRsp)
            return core;
        if( element instanceof ServerStateWrapper)
            return ((ServerStateWrapper)element).rsp;
        if( element instanceof DeployableStateWrapper)
            return ((DeployableStateWrapper)element).serverState;
        return null;
    }

    @NotNull
    @Override
    public PresentableNodeDescriptor createDescriptor(@NotNull Object element, @Nullable NodeDescriptor parentDescriptor) {
        if( element instanceof RspCore )
            return new CoreDescriptor((RspCore)element, parentDescriptor);
        if( element instanceof IRsp) {
            return new RspServerDescriptor((IRsp)element, parentDescriptor);
        }
        if( element instanceof ServerStateWrapper) {
            return new ServerStateDescriptor((ServerStateWrapper)element, parentDescriptor);
        }
        if( element instanceof DeployableStateWrapper) {
            return new DeployableStateDescriptor((DeployableStateWrapper)element, parentDescriptor);
        }
        return new StandardDescriptor((Object)element, parentDescriptor);
    }

    private interface InnerLabelProvider {
        public String getText();
    }

    private class CoreDescriptor extends Descriptor<RspCore> {
        protected CoreDescriptor(RspCore element, @Nullable NodeDescriptor parentDescriptor) {
            super(element, parentDescriptor, () -> "Runtime Server Protocol", IconLoader.getIcon("/images/community-12x24.png"));
        }
    }

    private class RspServerDescriptor extends Descriptor<IRsp> {
        protected RspServerDescriptor(IRsp element, @Nullable NodeDescriptor parentDescriptor) {
            super(element, parentDescriptor,
                    () -> getRspString(element),
                    element.getRspType().getIcon());
        }
    }

    private static String getRspString(IRsp element) {
        return element.getRspType().getName() + "   [" + getRspState(element) + "]";
    }
    private class ServerStateDescriptor extends Descriptor<ServerStateWrapper> {
        protected ServerStateDescriptor(ServerStateWrapper element, @Nullable NodeDescriptor parentDescriptor) {
            super(element, parentDescriptor,
                    () -> getServerStateString(element),
                    ((RspServerDescriptor)parentDescriptor).getElement().getRspType().getIcon(element.ss.getServer().getType().getId()));
        }
    }

    private static String getServerStateString(ServerStateWrapper element) {
        return element.ss.getServer().getId() + "   [" +
                getRunStateString(element.ss.getState()) + ", " +
                getPublishStateString(element.ss.getPublishState()) + "]";
    }

    private class DeployableStateDescriptor extends Descriptor<DeployableStateWrapper> {
        protected DeployableStateDescriptor(DeployableStateWrapper element, @Nullable NodeDescriptor parentDescriptor) {
            super(element, parentDescriptor, () ->
                            element.ds.getReference().getLabel() + "   [" +
                                    getRunStateString(element.ds.getState()) + ", " +
                                    getPublishStateString(element.ds.getPublishState()) + "]",
                    IconLoader.getIcon("images/jar_obj.gif"));
        }
    }

    private class StandardDescriptor extends Descriptor<Object> {
        protected StandardDescriptor(Object element, @Nullable NodeDescriptor parentDescriptor) {
            super(element, parentDescriptor, () -> element.toString(), AllIcons.General.Information);
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

    public static String getRspState(IRsp rsp) {
        IRspCore.IJServerState state = rsp.getState();
        if( state == IRspCore.IJServerState.STARTED && !rsp.wasLaunched()) {
            return "Connected";
        }
        return rsp.getState().toString().toUpperCase();
    }

    public static String getRunStateString(int state) {
        String stateString = "unknown";
        switch(state) {
            case ServerManagementAPIConstants.STATE_UNKNOWN:
                stateString = "unknown";
                break;
            case ServerManagementAPIConstants.STATE_STARTED:
                stateString = "started";
                break;
            case ServerManagementAPIConstants.STATE_STARTING:
                stateString = "starting";
                break;
            case ServerManagementAPIConstants.STATE_STOPPED:
                stateString = "stopped";
                break;
            case ServerManagementAPIConstants.STATE_STOPPING:
                stateString = "stopping";
                break;

        }
        return stateString.toUpperCase();
    }

    public static String getPublishTypeString(int type) {
        String stateString = "unknown";
        switch(type) {
            case ServerManagementAPIConstants.PUBLISH_AUTO:
                stateString = "auto";
                break;
            case ServerManagementAPIConstants.PUBLISH_FULL:
                stateString = "full";
                break;
            case ServerManagementAPIConstants.PUBLISH_INCREMENTAL:
                stateString = "incremental";
                break;
            case ServerManagementAPIConstants.PUBLISH_CLEAN:
                stateString = "clean";
                break;
        }
        return stateString.toUpperCase();
    }
    public static String getPublishStateString(int state) {
        String stateString = "unknown";
        switch(state) {
            case ServerManagementAPIConstants.PUBLISH_STATE_ADD:
                stateString = "add";
                break;
            case ServerManagementAPIConstants.PUBLISH_STATE_FULL:
                stateString = "publish required (full)";
                break;
            case ServerManagementAPIConstants.PUBLISH_STATE_INCREMENTAL:
                stateString = "publish required (incremental)";
                break;
            case ServerManagementAPIConstants.PUBLISH_STATE_NONE:
                stateString = "synchronized";
                break;
            case ServerManagementAPIConstants.PUBLISH_STATE_REMOVE:
                stateString = "remove";
                break;
            case ServerManagementAPIConstants.PUBLISH_STATE_UNKNOWN:
                stateString = "unknown";
                break;

        }
        return stateString.toUpperCase();
    }
}
