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
package com.redhat.devtools.intellij.ui.dialogs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.redhat.devtools.intellij.ui.tree.RspTreeModel;
import com.redhat.devtools.intellij.util.AlphanumComparator;
import org.jboss.tools.rsp.api.dao.ListServerActionResponse;
import org.jboss.tools.rsp.api.dao.ServerActionWorkflow;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class SelectServerActionDialog extends DialogWrapper implements ListSelectionListener {
    private final ListServerActionResponse actionResponse;
    private final RspTreeModel.ServerStateWrapper state;
    private HashMap<String, ServerActionWorkflow> dataMap;

    private JPanel contentPane;
    private JBList list;
    private ServerActionWorkflow selected = null;
    public SelectServerActionDialog(RspTreeModel.ServerStateWrapper state, ListServerActionResponse actionResponse) {
        super((Project)null, true, IdeModalityType.IDE);
        this.state = state;
        this.actionResponse = actionResponse;
        dataMap = new HashMap<>();
        for( ServerActionWorkflow descriptor : actionResponse.getWorkflows() ) {
            dataMap.put(descriptor.getActionId(), descriptor);
        }
        setTitle("Select a Server Action");
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        createLayout();
        return contentPane;
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        Object o = list.getSelectedValue();
        if( o instanceof ServerActionWorkflow) {
            selected = (ServerActionWorkflow)o;
        }
    }

    public ServerActionWorkflow getSelected() {
        return selected;
    }

    private class ServerActionCellRenderer extends AbstractRspCellRenderer {
        public ServerActionCellRenderer() {
            super();
        }

        @Override
        protected String getTextForValue(Object value) {
            if( value instanceof ServerActionWorkflow) {
                return (((ServerActionWorkflow) value).getActionLabel());
            }
            return null;
        }

        @Override
        protected Icon getIconForValue(Object value) {
                return null;
        }
    }

    private void createLayout() {
        java.util.List<ServerActionWorkflow> actions = new ArrayList<>(dataMap.values());
        Collections.sort(actions, (o1,o2) -> {
            return AlphanumComparator.staticCompare(o1.getActionLabel(), o2.getActionLabel());
        });
        list = new JBList(actions);
        ServerActionCellRenderer renderer = new ServerActionCellRenderer();
        list.setCellRenderer(renderer);
        list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setVisibleRowCount(15);
        list.addListSelectionListener(this);
        JScrollPane listScroller = new JBScrollPane(list);
        listScroller.setPreferredSize(new Dimension(250, 250));
        contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        JLabel instructions = new JLabel("Please select a server action to execute...");
        contentPane.add(instructions);
        contentPane.add(listScroller);
    }

}
