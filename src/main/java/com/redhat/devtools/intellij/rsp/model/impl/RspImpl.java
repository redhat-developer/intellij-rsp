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
package com.redhat.devtools.intellij.rsp.model.impl;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.rsp.model.*;
import com.redhat.devtools.intellij.rsp.client.IntelliJRspClientLauncher;
import com.redhat.devtools.intellij.rsp.download.DownloadUtility;
import com.redhat.devtools.intellij.rsp.download.UnzipUtility;
import com.redhat.devtools.intellij.rsp.ui.util.UIHelper;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The implementation for an RSP.
 * Most of this implementation should be fairly standard, with the
 * custom bits in the IRspStateController
 */
public class RspImpl implements IRsp, IRspStartCallback {
    private final IRspStateController controller;
    private final IRspCore model;
    private IRspType type;
    private String latestVersion;
    private String downloadUrl;
    private IRspCore.IJServerState currentState;
    private boolean launched;

    public RspImpl(IRspCore model, IRspType type,
                   String latestVersion, String downloadUrl,
                   IRspStateController controller) {
        this.model = model;
        this.type = type;
        this.latestVersion = latestVersion;
        this.downloadUrl = downloadUrl;
        this.controller = controller;
        this.currentState = exists() ?  IRspCore.IJServerState.STOPPED : IRspCore.IJServerState.MISSING;
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
    public String getLatestVersion() {
        return latestVersion;
    }

    @Override
    public String getInstalledVersion() {
        String home = getRspType().getServerHome();
        File dotVersion = new File(home, RspTypeImpl.FILE_DOT_VERSION);
        if (dotVersion.exists()) {
            String ret = readFileContent(dotVersion.toPath());
            return ret == null ? null : ret.trim();
        }
        return null;
    }

    private String readFileContent(Path filePath) {
        try {
            return new String ( Files.readAllBytes( filePath ) );
        } catch (IOException e) {
            // TODO log error
            return null;
        }
    }

    @Override
    public ServerConnectionInfo start() {
        try {
            return getController().start(this);
        } catch(StartupFailedException sfe) {
            UIHelper.executeInUI(() -> {
                Messages.showErrorDialog(sfe.getMessage(), "Unable to start RSP");
            });
            return null;
        }
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
            if( client.getServerProxy() != null ) {
                client.getServerProxy().shutdown();
                return;
            }
        }
        terminate();
    }

    @Override
    public IRspCore.IJServerState getState() {
        return currentState;
    }

    @Override
    public boolean wasLaunched() {
        return this.launched;
    }

    @Override
    public boolean exists() {
        File f = new File(this.type.getServerHome());
        boolean b = f.exists();
        return b;
    }

    @Override
    public void download() {
        boolean b = exists();
        if( !b && latestVersion != null && downloadUrl != null ) {
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
                            UnzipUtility util = new UnzipUtility(toDl);
                            util.extract(toExtract);
                            String root = util.getRoot();
                            File extractedRoot = toExtract.toPath().resolve(root).toFile();
                            File dotVersion = new File(extractedRoot, RspTypeImpl.FILE_DOT_VERSION);
                            if( !dotVersion.exists()) {
                                Files.write(dotVersion.toPath(), getLatestVersion().getBytes());
                            }
                        }
                    } catch(IOException ioe) {
                    }
                    if( exists() ) {
                        updateRspState(IRspCore.IJServerState.STOPPED);
                    } else {
                        updateRspState(IRspCore.IJServerState.MISSING);
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
        File dlFile = new File(downloads, getRspType().getId() + "-" + getLatestVersion() + ".zip");
        return dlFile;
    }
    protected IRspStateController getController() {
        return controller;
    }


    public void updateRspState(IRspCore.IJServerState state) {
        this.currentState = state;
        model.stateUpdated(this);
    }
    @Override
    public void updateRspState(IRspCore.IJServerState state, boolean launched) {
        this.currentState = state;
        this.launched = launched;
        model.stateUpdated(this);
    }
}
