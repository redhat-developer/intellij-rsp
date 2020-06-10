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
    private IRspCore.IJServerState currentState = IRspCore.IJServerState.STOPPED;

    public RspImpl(IRspCore model, IRspType type,
                   String latestVersion, String downloadUrl,
                   IRspStateController controller) {
        this.model = model;
        this.type = type;
        this.latestVersion = latestVersion;
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
        File f = new File(this.type.getServerHome());
        boolean b = f.exists();
        return b;
    }

    @Override
    public void download() {
        boolean b = exists();
        if( !b) {
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


    @Override
    public void updateRspState(IRspCore.IJServerState state) {
        this.currentState = state;
        model.stateUpdated(this);
    }
}
