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
import com.redhat.devtools.intellij.rsp.util.VersionComparatorUtil;
import com.redhat.devtools.intellij.rsp.model.IRsp;
import com.redhat.devtools.intellij.rsp.model.impl.RspTypeImpl;

import javax.swing.tree.TreePath;
import java.io.File;

public class DownloadRspAction extends AbstractTreeAction {

    @Override
    protected boolean isEnabled(Object[] o2) {
        Object o = (o2 != null && o2.length == 1 ? o2[0] : null);
        boolean isRSP = o instanceof IRsp;
        if( !isRSP )
            return false;

        boolean downloadMissing = ((IRsp)o).getState() == IRspCore.IJServerState.MISSING;
        if( downloadMissing )
            return true;

        IRsp server = (IRsp)o;
        String installed = server.getInstalledVersion();
        String latest = server.getLatestVersion();
        if( server.getLatestVersion() == null)
            return false;

        if( !server.exists() || installed == null ||
                VersionComparatorUtil.isGreaterThan(latest, installed.trim())) {
            return true;
        }

        return false;
    }
    protected boolean isVisible(Object[] o) {
        return safeSingleItemClass(o, IRsp.class);
    }

    @Override
    protected void singleSelectionActionPerformed(AnActionEvent e, TreePath treePath, Object selected) {
        if( selected instanceof IRsp) {
            IRsp server = (IRsp)selected;
            String installed = server.getInstalledVersion();
            String latest = server.getLatestVersion();
            if( !server.exists() || installed == null || VersionComparatorUtil.isGreaterThan(latest, installed.trim())) {
                String home = server.getRspType().getServerHome();
                new Thread("Updating RSP " + server.getRspType().getName()) {
                    public void run() {
                        deleteDirectory(RspTypeImpl.getServerTypeInstallLocation(server.getRspType()));
                        server.download();
                    }
                }.start();
            }
        }
    }

    boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }
}
