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
package org.jboss.tools.intellij.rsp.ui.dialogs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jboss.tools.rsp.api.dao.Attributes;
import org.jboss.tools.rsp.api.dao.WorkflowResponseItem;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.HashMap;
import java.util.Map;

public class WorkflowDialog extends DialogWrapper  {
    private Map<String, Object> attributeValues;
    private JPanel contentPane;
    private WorkflowResponseItem[] items;
    private WorkflowItemsPanel panel;

    public WorkflowDialog(WorkflowResponseItem[] items) {
        super((Project)null, true, IdeModalityType.IDE);
        this.items = items;
        this.attributeValues = new HashMap<String, Object>();
        setTitle("Download Runtime...");
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        createLayout();
        return contentPane;
    }

    private void createLayout() {
        WorkflowItemsPanel itemsPanel = new WorkflowItemsPanel(items, null, attributeValues);
        contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        contentPane.add(itemsPanel);
    }

    public Map<String, Object> getAttributes() {
        return attributeValues;
    }
}
