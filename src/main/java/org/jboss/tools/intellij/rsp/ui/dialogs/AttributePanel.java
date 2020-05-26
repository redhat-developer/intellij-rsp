package org.jboss.tools.intellij.rsp.ui.dialogs;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.Attribute;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.Map;

public class AttributePanel extends JPanel implements DocumentListener {
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
        field = new JTextField();
        if( values.get(key) != null ) {
            field.setText(asString(oneAttribute.getType(), values.get(key)));
        } else if( attr.getDefaultVal() != null ) {
            field.setText(asString(oneAttribute.getType(), attr.getDefaultVal()));
        }
        add(field);
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
