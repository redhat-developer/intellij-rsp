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
package org.jboss.tools.intellij.rsp.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jboss.tools.rsp.api.ServerManagementAPIConstants;

import javax.swing.tree.TreePath;

public class FullPublishServerAction extends IncrementalPublishServerAction {
    @Override
    protected void actionPerformed(AnActionEvent e, TreePath treePath, Object selected) {
        actionPerformedInternal(e, treePath, selected, ServerManagementAPIConstants.PUBLISH_FULL);
    }
}
