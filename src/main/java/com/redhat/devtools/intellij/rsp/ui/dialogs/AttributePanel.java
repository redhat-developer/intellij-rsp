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

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.Attribute;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

public class AttributePanel extends JPanel implements DocumentListener {
    private static final int TEXTFIELD_MAX_SIZE=10;
    private final Attribute attr;
    private String key;
    private Map<String, Object> values;
    private JTextField field;

    public AttributePanel(String key, Attribute oneAttribute, Map<String, Object> values) {
        this.attr = oneAttribute;
        this.key = key;
        this.values = values;
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        JLabel name = new JLabel(key);
        name.setToolTipText(attr.getDescription());
        add(name);

        Object valueObj = values.get(key);
        String valueStr = (valueObj == null ? "" : valueObj.toString());

        if( oneAttribute.getType().equals(ServerManagementAPIConstants.ATTR_TYPE_LOCAL_FILE )) {
            field = new JTextField(TEXTFIELD_MAX_SIZE/2);
            if( valueStr != null ) field.setText(valueStr);
            JButton button = new JButton("Browse...");
            add(field);
            add(button);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Project project = ProjectManager.getInstance().getOpenProjects()[0];
                    final FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleLocalFileDescriptor();
                    final VirtualFile result = FileChooser.chooseFile(descriptor, project, null);
                    VirtualFile vf1 = result == null ? null : result;
                    if( vf1 != null ) {
                        field.setText(vf1.getPath());
                    }
                }
            });
        } else if( oneAttribute.getType().equals(ServerManagementAPIConstants.ATTR_TYPE_LOCAL_FOLDER )) {
            field = new JTextField(TEXTFIELD_MAX_SIZE/2);
            if( valueStr != null ) field.setText(valueStr);
            JButton button = new JButton("Browse...");
            add(field);
            add(button);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Project project = ProjectManager.getInstance().getOpenProjects()[0];
                    final FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
                    final VirtualFile result = FileChooser.chooseFile(descriptor, project, null);
                    VirtualFile vf1 = result == null ? null : result;
                    if( vf1 != null ) {
                        field.setText(vf1.getPath());
                    }
                }
            });
        } else {
            field = oneAttribute.isSecret() ? new JPasswordField(TEXTFIELD_MAX_SIZE) : new JTextField(TEXTFIELD_MAX_SIZE);
            if (values.get(key) != null) {
                field.setText(asString(oneAttribute.getType(), values.get(key)));
            } else if (attr.getDefaultVal() != null) {
                field.setText(asString(oneAttribute.getType(), attr.getDefaultVal()));
            }
            add(field);
        }


        field.getDocument().addDocumentListener(this);
    }

    private String asString(String type, Object value) {
        if(ServerManagementAPIConstants.ATTR_TYPE_BOOL.equals(type)) {
            return value == null ? "false" : Boolean.toString("true".equalsIgnoreCase(value.toString()));
        }
        if(ServerManagementAPIConstants.ATTR_TYPE_INT.equals(type)) {
            if( value instanceof Number ) {
                return Integer.toString(((Number)value).intValue());
            } else {
                return Integer.toString(new Double(Double.parseDouble(value.toString())).intValue());
            }
        }
        return value.toString();
    }

    private Object asObject(String text) {
        String type = attr.getType();
        if(ServerManagementAPIConstants.ATTR_TYPE_BOOL.equals(type)) {
            return Boolean.parseBoolean(text);
        }
        if(ServerManagementAPIConstants.ATTR_TYPE_INT.equals(type)) {
            try {
                return Integer.parseInt(text);
            } catch(NumberFormatException nfe) {
                return null;
            }
        }
        return text;
    }


    @Override
    public void insertUpdate(DocumentEvent e) {
        values.put(key,asObject(field.getText()));
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        values.put(key,asObject(field.getText()));
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        values.put(key,asObject(field.getText()));
    }
}
