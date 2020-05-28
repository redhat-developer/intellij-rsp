package org.jboss.tools.intellij.rsp.ui.dialogs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import org.jboss.tools.intellij.rsp.model.IRsp;
import org.jboss.tools.intellij.rsp.model.IRspType;
import org.jboss.tools.intellij.rsp.model.impl.RspCore;
import org.jboss.tools.intellij.rsp.util.AlphanumComparator;
import org.jboss.tools.rsp.api.dao.DownloadRuntimeDescription;
import org.jboss.tools.rsp.api.dao.ListDownloadRuntimeResponse;
import org.jetbrains.annotations.Nullable;
import sun.swing.DefaultLookup;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class SelectDownloadRuntimeDialog extends DialogWrapper implements ListSelectionListener {
    private final ListDownloadRuntimeResponse runtimeResponse;
    private final IRsp rsp;
    private HashMap<String, DownloadRuntimeDescription> dataMap;
    private JPanel contentPane;
    private JBList list;
    private DownloadRuntimeDescription selected = null;
    public SelectDownloadRuntimeDialog(IRsp rsp, ListDownloadRuntimeResponse runtimeResponse) {
        super((Project)null, true, IdeModalityType.IDE);
        this.rsp = rsp;
        this.runtimeResponse = runtimeResponse;
        dataMap = new HashMap<>();
        for( DownloadRuntimeDescription descriptor : runtimeResponse.getRuntimes() ) {
            dataMap.put(descriptor.getId(), descriptor);
        }
        setTitle("Download Server Runtime...");
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
        if( o instanceof DownloadRuntimeDescription) {
            selected = (DownloadRuntimeDescription)o;
        }
    }

    public DownloadRuntimeDescription getSelected() {
        return selected;
    }

    private class DownloadRuntimeCellRenderer extends DefaultListCellRenderer {
        private final Border SAFE_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);
        private final Border DEFAULT_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);
        protected Border noFocusBorder = DEFAULT_NO_FOCUS_BORDER;

        public DownloadRuntimeCellRenderer() {
            super();
        }


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

                bg = DefaultLookup.getColor(this, ui, "List.dropCellBackground");
                fg = DefaultLookup.getColor(this, ui, "List.dropCellForeground");

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

            if( value instanceof DownloadRuntimeDescription) {
                setText(((DownloadRuntimeDescription)value).getName());
                Icon found = findIcon((DownloadRuntimeDescription) value);
                if( found != null ) {
                    setIcon(found);
                }
            }



            setEnabled(list.isEnabled());
            setFont(list.getFont());

            Border border = null;
            if (cellHasFocus) {
                if (isSelected) {
                    border = DefaultLookup.getBorder(this, ui, "List.focusSelectedCellHighlightBorder");
                }
                if (border == null) {
                    border = DefaultLookup.getBorder(this, ui, "List.focusCellHighlightBorder");
                }
            } else {
                border = getNoFocusBorder();
            }
            setBorder(border);

            return this;
        }

        private Border getNoFocusBorder() {
            Border border = DefaultLookup.getBorder(this, ui, "List.cellNoFocusBorder");
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
    }

    private Icon findIcon(DownloadRuntimeDescription desc) {
        String serverType = desc.getProperties().get("wtp-runtime-type");
        return rsp.getRspType().getIcon(serverType);
    }

    private void createLayout() {
        java.util.List<DownloadRuntimeDescription> dlrts = new ArrayList<>(dataMap.values());
        Collections.sort(dlrts, (o1,o2) -> {
            return AlphanumComparator.staticCompare(o1.getName(), o2.getName());
        });
        list = new JBList(dlrts);
        DownloadRuntimeCellRenderer renderer = new DownloadRuntimeCellRenderer();
        list.setCellRenderer(renderer);
        list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setVisibleRowCount(15);
        list.addListSelectionListener(this);
        JScrollPane listScroller = new JBScrollPane(list);
        listScroller.setPreferredSize(new Dimension(250, 250));
        contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        JLabel instructions = new JLabel("Please select a runtime to download...");
        contentPane.add(instructions);
        contentPane.add(listScroller);
    }

}
