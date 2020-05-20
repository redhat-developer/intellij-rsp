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
package org.jboss.tools.intellij.rsp.model.impl;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import org.jboss.tools.intellij.rsp.client.IntelliJRspClientLauncher;
import org.jboss.tools.intellij.rsp.download.DownloadUtility;
import org.jboss.tools.intellij.rsp.download.UnzipUtility;
import org.jboss.tools.intellij.rsp.model.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class RspImpl implements IRsp, IRspStartCallback {
    private final IRspStateController controller;
    private final IRspCore model;
    private IRspType type;
    private String version;
    private String downloadUrl;
    private IRspCore.IJServerState currentState = IRspCore.IJServerState.STOPPED;

    public RspImpl(IRspCore model, IRspType type,
                   String version, String downloadUrl,
                   IRspStateController controller) {
        this.model = model;
        this.type = type;
        this.version = version;
        this.downloadUrl = downloadUrl;
        this.controller = controller;
    }

    @Override
    public IRspCore getModel() {
        return model;
    }
    @Override
    public IRspType getRspType() {
        return type;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public ServerConnectionInfo start() {
        return getController().start(this);
    }

    @Override
    public void terminate() {
        getController().terminate(this);
    }

    @Override
    public void stop() {
        updateRspState(IRspCore.IJServerState.STOPPING);
        IntelliJRspClientLauncher client = model.getClient(this);
        if( client != null ) {
            client.getServerProxy().shutdown();
        } else {
            terminate();
        }
    }

    @Override
    public IRspCore.IJServerState getState() {
        return currentState;
    }

    @Override
    public boolean exists() {
        return new File(this.type.getServerHome()).exists();
    }

    @Override
    public void download() {
        if( !exists()) {
            final String serverHome = this.type.getServerHome();
            ProgressManager.getInstance().run(new Task.Backgroundable(null, "Downloading " + getRspType().getName()) {

                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    File toDl = getRspDownloadLocation();
                    toDl.getParentFile().mkdirs();
                    File toExtract = new File(serverHome);
                    try {
                        new DownloadUtility().download(downloadUrl, toDl.toPath(), indicator);
                        if( toDl.exists()) {
                            new UnzipUtility(toDl).extract(toExtract);
                        }
                    } catch(IOException ioe) {
                    }
                }
            });
        }
    }


    protected File getRspDownloadLocation() {
        File home = new File(System.getProperty(RspTypeImpl.SYSPROP_USER_HOME));
        File root = new File(home, RspTypeImpl.DATA_LOCATION_DEFAULT);
        File installs = new File(root, RspTypeImpl.INSTALLATIONS);
        File downloads = new File(installs, RspTypeImpl.DOWNLOADS);
        File dlFile = new File(downloads, getRspType().getId() + "-" + getVersion() + ".zip");
        return dlFile;
    }
    protected IRspStateController getController() {
        return controller;
    }


    @Override
    public void updateRspState(IRspCore.IJServerState state) {
        this.currentState = state;
        model.stateUpdated(this);
    }
}
