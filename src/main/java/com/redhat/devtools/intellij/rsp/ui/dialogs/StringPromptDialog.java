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
package com.redhat.devtools.intellij.rsp.ui.dialogs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.ui.components.JBTextField;
import com.redhat.devtools.intellij.rsp.model.IRsp;
import org.jboss.tools.rsp.api.dao.StringPrompt;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class StringPromptDialog extends DialogWrapper implements DocumentListener {
    private JPanel contentPane;
    private IRsp rsp;
    private StringPrompt stringPrompt;
    private JTextField field;
    private String fieldVal = "";

    public StringPromptDialog(IRsp rsp, StringPrompt stringPrompt) {
        super((Project)null, true, IdeModalityType.IDE);
        this.rsp = rsp;
        this.stringPrompt = stringPrompt;
        setTitle("Prompt from " + rsp.getRspType().getName());
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        createLayout();
        return contentPane;
    }

    private void createLayout() {
        contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        contentPane.add(new JLabel(stringPrompt.getPrompt()));
        if( stringPrompt.isSecret()) {
            field = new JBPasswordField();
        } else {
            field = new JBTextField();
        }

        contentPane.add(field);

        field.getDocument().addDocumentListener(this);
    }

    public String getText() {
        return fieldVal;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        fieldVal = field.getText();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        fieldVal = field.getText();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        fieldVal = field.getText();
    }
}
