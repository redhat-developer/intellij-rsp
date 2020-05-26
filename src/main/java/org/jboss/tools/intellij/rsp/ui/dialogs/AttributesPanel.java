package org.jboss.tools.intellij.rsp.ui.dialogs;

import org.jboss.tools.rsp.api.dao.Attribute;
import org.jboss.tools.rsp.api.dao.Attributes;

import javax.swing.*;
import java.util.Map;

public class AttributesPanel extends JPanel {
    private Attributes attr;
    private String title;
    private Map<String, Object> values;
    public AttributesPanel(Attributes attributes, String title, Map<String, Object> values) {
        this.attr = attributes;
        this.title = title;
        this.values = values;
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(title),
                BorderFactory.createEmptyBorder(5,5,5,5)));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        Map<String, Attribute> map = attributes.getAttributes();
        for( String key : map.keySet()) {
            Attribute oneAttribute = map.get(key);
            add(new AttributePanel(key, oneAttribute, values));
        }
    }
}
