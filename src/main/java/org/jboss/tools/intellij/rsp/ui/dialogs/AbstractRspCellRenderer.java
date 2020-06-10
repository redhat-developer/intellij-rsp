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

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ComponentUI;
import java.awt.*;

/**
 * Code copied from intelli-j internal classes for use in rendering cells in lists.
 */
public abstract class AbstractRspCellRenderer extends DefaultListCellRenderer {
    private final Border SAFE_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);
    private final Border DEFAULT_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);
    protected Border noFocusBorder = DEFAULT_NO_FOCUS_BORDER;

    public AbstractRspCellRenderer() {
        super();
    }

    protected abstract String getTextForValue(Object value);
    protected abstract Icon getIconForValue(Object value);

    public Component getListCellRendererComponent(
            JList<?> list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus)
    {
        setComponentOrientation(list.getComponentOrientation());

        Color bg = null;
        Color fg = null;

        JList.DropLocation dropLocation = list.getDropLocation();
        if (dropLocation != null
                && !dropLocation.isInsert()
                && dropLocation.getIndex() == index) {

            bg = getColor(this, ui, "List.dropCellBackground");
            fg = getColor(this, ui, "List.dropCellForeground");

            isSelected = true;
        }

        if (isSelected) {
            setBackground(bg == null ? list.getSelectionBackground() : bg);
            setForeground(fg == null ? list.getSelectionForeground() : fg);
        }
        else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        String txt = getTextForValue(value);
        if( txt != null )
            setText(txt);
        Icon icon = getIconForValue(value);
        if( icon != null )
            setIcon(icon);

        setEnabled(list.isEnabled());
        setFont(list.getFont());

        Border border = null;
        if (cellHasFocus) {
            if (isSelected) {
                border = getBorder(this, ui, "List.focusSelectedCellHighlightBorder");
            }
            if (border == null) {
                border = getBorder(this, ui, "List.focusCellHighlightBorder");
            }
        } else {
            border = getNoFocusBorder();
        }
        setBorder(border);

        return this;
    }

    private Border getNoFocusBorder() {
        Border border = getBorder(this, ui, "List.cellNoFocusBorder");
        if (System.getSecurityManager() != null) {
            if (border != null) return border;
            return SAFE_NO_FOCUS_BORDER;
        } else {
            if (border != null &&
                    (noFocusBorder == null ||
                            noFocusBorder == DEFAULT_NO_FOCUS_BORDER)) {
                return border;
            }
            return noFocusBorder;
        }
    }

    public static Color getColor(JComponent c, ComponentUI ui, String key,
                                 Color defaultValue) {
        Object iValue = UIManager.get(key, c.getLocale());
        if (iValue == null || !(iValue instanceof Color)) {
            return defaultValue;
        }
        return (Color)iValue;
    }

    public static Color getColor(JComponent c, ComponentUI ui, String key) {
        return getColor(c, ui, key, null);
    }

    public static Border getBorder(JComponent c, ComponentUI ui, String key,
                                   Border defaultValue) {
        Object iValue = UIManager.get(key, c.getLocale());
        if (iValue == null || !(iValue instanceof Border)) {
            return defaultValue;
        }
        return (Border)iValue;
    }

    public static Border getBorder(JComponent c, ComponentUI ui, String key) {
        return getBorder(c, ui, key, null);
    }

}
