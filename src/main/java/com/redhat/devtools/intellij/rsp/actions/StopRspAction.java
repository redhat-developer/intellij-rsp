/*******************************************************************************
 * Copyright (c) 2019-2020 Red Hat, Inc.
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
import com.redhat.devtools.intellij.rsp.model.IRspCore;
import com.redhat.devtools.intellij.rsp.model.impl.RspCore;
import com.redhat.devtools.intellij.rsp.model.IRsp;

import javax.swing.tree.TreePath;

public class StopRspAction extends AbstractTreeAction {
    @Override
    protected boolean isVisible(Object[] o) {
        return safeSingleItemClass(o, IRsp.class);
    }

    @Override
    protected boolean isEnabled(Object[] o) {
        return safeSingleItemClass(o, IRsp.class) && ((IRsp)o[0]).getState() != IRspCore.IJServerState.STOPPED && ((IRsp)o[0]).exists();
    }

    @Override
    protected void singleSelectionActionPerformed(AnActionEvent e, TreePath treePath, Object selected) {
        if( selected instanceof IRsp) {
            IRsp server = (IRsp)selected;
            new Thread("Start RSP Server: " + server.getRspType().getId()) {
                public void run() {
                    RspCore.getDefault().stopServer(server);
                }
            }.start();
        }
    }

}
