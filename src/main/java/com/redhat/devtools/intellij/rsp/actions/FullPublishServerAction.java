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
package com.redhat.devtools.intellij.rsp.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jboss.tools.rsp.api.ServerManagementAPIConstants;

import javax.swing.tree.TreePath;

public class FullPublishServerAction extends IncrementalPublishServerAction {
    @Override
    protected void actionPerformed(AnActionEvent e, TreePath[] treePath, Object[] selected) {
        if( treePath.length == 1 && selected.length == 1 )
            actionPerformedInternal(e, treePath[0], selected[0], ServerManagementAPIConstants.PUBLISH_FULL);
    }
}
