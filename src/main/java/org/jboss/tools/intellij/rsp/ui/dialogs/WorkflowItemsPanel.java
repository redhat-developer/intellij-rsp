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

import org.jboss.tools.rsp.api.dao.WorkflowResponseItem;

import javax.swing.*;
import java.util.Map;

public class WorkflowItemsPanel extends JPanel {
    private WorkflowResponseItem[] items;
    private String title;
    private Map<String, Object> values;
    public WorkflowItemsPanel(WorkflowResponseItem[] items, String title, Map<String, Object> values) {
        this.items = items;
        this.title = title;
        this.values = values;
        if( title != null ) {
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder(title),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        }
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        for( int i = 0; i < items.length; i++ ) {
            add(new WorkflowItemPanel(items[i], values));
        }
    }
}
