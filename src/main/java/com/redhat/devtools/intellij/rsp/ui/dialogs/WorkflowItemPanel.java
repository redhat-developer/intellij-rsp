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
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.ui.components.JBScrollPane;
import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.WorkflowPromptDetails;
import org.jboss.tools.rsp.api.dao.WorkflowResponseItem;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

public class WorkflowItemPanel extends JPanel implements DocumentListener, ActionListener {
    public static final String WORKFLOW_ITEM_STRING_PROPERTY_LINK_URL = "workflow.item.string.property.link.url";

    private static final String COMBO_TRUE = "Yes (true)";
    private static final String COMBO_FALSE = "No (false)";

    private final WorkflowResponseItem item;
    private IWorkflowItemListener listener;
    private Map<String, Object> values;
    private ComboBox box;
    private JTextField field;

    public WorkflowItemPanel(WorkflowResponseItem item, Map<String, Object> values, IWorkflowItemListener listener) {
        this.item = item;
        this.values = values;
        this.listener = listener;
        String type = item.getItemType();
        String content = item.getContent();
        String msg = item.getLabel() + (content == null || content.isEmpty() ? "" : "\n" + content);

        if( type == null || "workflow.prompt.small".equals(type)) {
            handleSmall(item, msg);
        } else if( "workflow.prompt.large".equals(type)) {
            handleLarge(item, msg);
        }

    }

    private void handleSmall(WorkflowResponseItem item, String msg) {

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        String linkedUrl = null;
        if(item.getProperties() != null && item.getProperties().get(WORKFLOW_ITEM_STRING_PROPERTY_LINK_URL) != null ) {
            linkedUrl = item.getProperties().get(WORKFLOW_ITEM_STRING_PROPERTY_LINK_URL);
        }
        if (msg != null && !msg.isEmpty()) {
            JLabel l = new JLabel(msg);
            if( linkedUrl != null ) {
                linkLabelToUrl(l, linkedUrl);
            }
            add(l);
        }
        handleInput(item, values);
    }

    private void linkLabelToUrl(JLabel jl, String url) {
        jl.setForeground(Color.BLUE.darker());
        jl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        jl.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(url));
                } catch (IOException ioException) {
                } catch (URISyntaxException uriSyntaxException) {
                }
            }
        });
        jl.setText("<html><a href=''>" + jl.getText() + "</a></html>");
    }
    private void handleLarge(WorkflowResponseItem item, String msg) {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        if (msg != null && !msg.isEmpty()) {
            add(createTextArea(msg));
        }
        handleInput(item, values);
    }

    private JBScrollPane createTextArea(String msg) {
        JTextArea jta = new JTextArea(40,80);
        jta.setEditable(false);
        if( msg != null )
            jta.setText(msg);
        jta.setLineWrap(true);

        JBScrollPane scroll = new JBScrollPane(jta);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setValue(0);
        jta.setCaretPosition(0);
        return scroll;
    }

    private void handleInput(WorkflowResponseItem item, Map<String, Object> values) {
        WorkflowPromptDetails details = item.getPrompt();
        if( details != null ) {
            if( details.getResponseType().equals(ServerManagementAPIConstants.ATTR_TYPE_BOOL)) {
                String[] vals = new String[]{COMBO_TRUE, COMBO_FALSE };
                box = new ComboBox(vals);
                box.setSelectedIndex(-1);
                box.addActionListener(this);
                box.setMaximumSize(new Dimension(Integer.MAX_VALUE, box.getMinimumSize().height));
                add(box);
            }  else if( details.getResponseType().equals(ServerManagementAPIConstants.ATTR_TYPE_INT)) {
                List<String> valid = item.getPrompt().getValidResponses();
                if( valid == null || valid.size() == 0 ) {
                    field = item.getPrompt().isResponseSecret() ? new JBPasswordField() : new JTextField();
                    field.getDocument().addDocumentListener(this);
                    field.setMaximumSize(new Dimension(Integer.MAX_VALUE, field.getMinimumSize().height));
                    add(field);
                } else {
                    String[] vals = valid.toArray(new String[0]);
                    box = new ComboBox(vals);
                    box.setSelectedIndex(-1);
                    box.addActionListener(this);
                    box.setMaximumSize(new Dimension(Integer.MAX_VALUE, box.getMinimumSize().height));
                    add(box);
                }
            }  else if( details.getResponseType().equals(ServerManagementAPIConstants.ATTR_TYPE_STRING)) {
                List<String> valid = item.getPrompt().getValidResponses();
                if( valid == null || valid.size() == 0 ) {
                    field = item.getPrompt().isResponseSecret() ? new JBPasswordField() : new JTextField();
                    field.getDocument().addDocumentListener(this);
                    field.setMaximumSize(new Dimension(Integer.MAX_VALUE, field.getMinimumSize().height));
                    add(field);
                } else {
                    String[] vals = valid.toArray(new String[0]);
                    box = new ComboBox(vals);
                    box.setSelectedIndex(-1);
                    box.addActionListener(this);
                    box.setMaximumSize(new Dimension(Integer.MAX_VALUE, box.getMinimumSize().height));
                    add(box);
                }
            } else if( details.getResponseType().equals(ServerManagementAPIConstants.ATTR_TYPE_LOCAL_FILE)) {
                field = new JTextField();
                field.getDocument().addDocumentListener(this);
                field.setColumns(30);
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
            } else if( details.getResponseType().equals(ServerManagementAPIConstants.ATTR_TYPE_LOCAL_FOLDER)) {
                field = new JTextField();
                field.setColumns(30);
                field.getDocument().addDocumentListener(this);
                JButton button = new JButton("Browse...");
                add(field);
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
            }
        }
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
        String type = item.getPrompt().getResponseType();
        if(ServerManagementAPIConstants.ATTR_TYPE_BOOL.equals(type)) {
            if( COMBO_TRUE.equals(text))
                return Boolean.TRUE;
            if( COMBO_FALSE.equals(text))
                return Boolean.FALSE;
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
        values.put(item.getId(), asObject(getWidgetString()));
        listener.panelChanged();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        values.put(item.getId(), asObject(getWidgetString()));
        listener.panelChanged();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        values.put(item.getId(), asObject(getWidgetString()));
        listener.panelChanged();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        values.put(item.getId(), asObject(getWidgetString()));
        listener.panelChanged();
    }

    private String getWidgetString() {
        return box == null ? field.getText() : box.getSelectedItem() == null ? null : box.getSelectedItem().toString();
    }
}
