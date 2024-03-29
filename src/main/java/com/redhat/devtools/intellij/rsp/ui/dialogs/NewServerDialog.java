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

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.redhat.devtools.intellij.rsp.actions.AbstractTreeAction;
import com.redhat.devtools.intellij.rsp.client.IntelliJRspClientLauncher;
import com.redhat.devtools.intellij.rsp.telemetry.TelemetryService;
import com.redhat.devtools.intellij.rsp.ui.util.UIHelper;
import org.jboss.tools.rsp.api.dao.Attributes;
import org.jboss.tools.rsp.api.dao.CreateServerResponse;
import org.jboss.tools.rsp.api.dao.ServerAttributes;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class NewServerDialog extends DialogWrapper implements DocumentListener {
    private static final String ERROR_CREATING_SERVER = "Error creating server";

    private IntelliJRspClientLauncher client;
    private String typeId;
    private final Attributes optional;
    private final Attributes required;
    private AttributesPanel requiredPanel;
    private AttributesPanel optionalPanel;
    private Map<String, Object> attributeValues;
    private JTextField nameField;
    private String fName;
    private JPanel contentPane;
    public NewServerDialog(IntelliJRspClientLauncher client, String typeId, Attributes required, Attributes optional, Map<String, Object> values) {
        super((Project)null, true, IdeModalityType.IDE);
        this.client = client;
        this.typeId = typeId;
        this.required = required;
        this.optional = optional;
        this.attributeValues = values;
        setTitle("Create a new Server...");
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        createLayout();
        return contentPane;
    }

    private void createLayout() {
        requiredPanel = new AttributesPanel(required, "Required Attributes", attributeValues);
        optionalPanel = new AttributesPanel(optional, "Optional Attributes", attributeValues);
        getOKAction().setEnabled(false);
        contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        JPanel nameWrapper = new JPanel();
        contentPane.add(nameWrapper);
        nameWrapper.setLayout(new BoxLayout(nameWrapper, BoxLayout.X_AXIS));
        JLabel name = new JLabel("Server Name: ");
        nameWrapper.add(name);
        nameField = new JTextField();
        nameWrapper.add(nameField);

        if( required != null && required.getAttributes().size() > 0 )
            contentPane.add(requiredPanel);
        if( optional != null && optional.getAttributes().size() > 0 )
            contentPane.add(optionalPanel);
        nameField.getDocument().addDocumentListener(this);
        nameField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent documentEvent) {
                String nameText = nameField.getText();
                getOKAction().setEnabled(nameText != null && nameText.trim().length() > 0);
            }

            @Override
            public void removeUpdate(DocumentEvent documentEvent) {
                String nameText = nameField.getText();
                getOKAction().setEnabled(nameText != null && nameText.trim().length() > 0);
            }

            @Override
            public void changedUpdate(DocumentEvent documentEvent) {
                String nameText = nameField.getText();
                getOKAction().setEnabled(nameText != null && nameText.trim().length() > 0);
            }
        });
    }

    public String getName() {
        return fName;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        fName = nameField.getText();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        fName = nameField.getText();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        fName = nameField.getText();
    }

    protected void doOKAction() {
        if (getOKAction().isEnabled()) {
            getOKAction().setEnabled(false);
            new Thread("Create Server") {
                public void run() {
                    ServerAttributes csa = new ServerAttributes(typeId, fName, attributeValues);
                    try {
                        CreateServerResponse result = client.getServerProxy().createServer(csa).get();
                        TelemetryService.instance().sendWithType(TelemetryService.TELEMETRY_SERVER_CREATE, typeId, result.getStatus());
                        if (!result.getStatus().isOK()) {
                            UIHelper.executeInUIAsync(() -> {
                                getOKAction().setEnabled(true);
                                AbstractTreeAction.statusError(result.getStatus(), ERROR_CREATING_SERVER);
                            }, contentPane);
                        } else {
                            UIHelper.executeInUIAsync(() -> close(OK_EXIT_CODE), contentPane);
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        AbstractTreeAction.apiError(e, ERROR_CREATING_SERVER);
                        TelemetryService.instance().sendWithType(TelemetryService.TELEMETRY_SERVER_CREATE, typeId, e);
                    }
                }
            }.start();
        }
    }

}
