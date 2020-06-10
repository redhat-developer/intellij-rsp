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

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import org.jboss.tools.rsp.api.dao.Attribute;
import org.jboss.tools.rsp.api.dao.Attributes;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

public class AddDeploymentDialog extends DialogWrapper {
    private Attributes attributes;
    private AttributesPanel attributesPanel;
    private LocationPanel locationPanel;
    private Map<String, Object> attributeValues;
    private JPanel contentPane;

    public AddDeploymentDialog(Attributes attr, Map<String, Object> values) {
        super((Project)null, true, IdeModalityType.IDE);
        this.attributes = attr;
        this.attributeValues = values;
        setTitle("Add a deployment");
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        createLayout();
        return contentPane;
    }

    private void createLayout() {
        attributesPanel = new AttributesPanel(attributes, "Optional Attributes", attributeValues);
        locationPanel = new LocationPanel();
        contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        contentPane.add(locationPanel);

        if( attributes.getAttributes().size() > 0 )
            contentPane.add(attributesPanel);
    }

    public String getLabel() {
        return locationPanel.getPath();
    }

    public String getPath() {
        return locationPanel.getPath();
    }

    public class LocationPanel extends JPanel implements DocumentListener {
        String val;
        JTextField field;
        JButton button;
        public LocationPanel() {
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            JLabel name = new JLabel("Deployment Path");
            button = new JButton("Browse...");
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Project project = ProjectManager.getInstance().getOpenProjects()[0];
                    final FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
                    final VirtualFile[] result = FileChooser.chooseFiles(descriptor, project, null);
                    VirtualFile vf1 = result == null || result.length == 0 ? null : result[0];
                    if( vf1 != null ) {
                        field.setText(vf1.getPath());
                        val = vf1.getPath();
                    }
                }
            });
            field = new JTextField();
            field.setPreferredSize(new Dimension(150, (int)field.getPreferredSize().getHeight()));
            field.getDocument().addDocumentListener(this);
            add(name);
            add(field);
            add(button);
        }

        public String getPath() {
            return val;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            updateAllPostEvent();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            updateAllPostEvent();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            updateAllPostEvent();
        }

        private void updateAllPostEvent() {
            val = field.getText();
        }
    }

}
