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

import org.jboss.tools.intellij.rsp.client.IntelliJRspClientLauncher;
import org.jboss.tools.intellij.rsp.model.*;

public class RspImpl implements IRsp, IRspStartCallback {
    private final IRspStateController controller;
    private final IRspCore model;
    private IRspType type;
    private String version;
    private String home;
    private IRspCore.IJServerState currentState = IRspCore.IJServerState.STOPPED;

    public RspImpl(IRspCore model,
                   IRspType type, String version,
                   String home, IRspStateController controller) {
        this.model = model;
        this.type = type;
        this.version = version;
        this.controller = controller;
    }

    @Override
    public IRspCore getModel() {
        return model;
    }
    @Override
    public IRspType getServerType() {
        return type;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getServerHome() {  return home; }

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

    protected IRspStateController getController() {
        return controller;
    }


    @Override
    public void updateRspState(IRspCore.IJServerState state) {
        this.currentState = state;
        model.stateUpdated(this);
    }
}
