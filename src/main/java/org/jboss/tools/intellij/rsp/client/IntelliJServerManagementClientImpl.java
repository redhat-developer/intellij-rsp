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
package org.jboss.tools.intellij.rsp.client;

import org.jboss.tools.intellij.rsp.model.IRspServer;
import org.jboss.tools.rsp.api.RSPClient;
import org.jboss.tools.rsp.api.RSPServer;
import org.jboss.tools.rsp.api.dao.*;

import java.util.concurrent.CompletableFuture;

public class IntelliJServerManagementClientImpl implements RSPClient {
    private final IRspServer uiRspServer;
    private RSPServer server;
    public IntelliJServerManagementClientImpl(IRspServer rspUi) {
        this.uiRspServer = rspUi;
    }

    public void initialize(RSPServer server) {
        this.server = server;
    }

    public RSPServer getProxy() {
        return server;
    }

    private void refreshView() {
        uiRspServer.getModel().modelUpdated(uiRspServer);
    }

    private void sendServerOutputToConsole(ServerProcessOutput serverProcessOutput) {
        // TODO do this
    }

    @Override
    public void jobAdded(JobHandle jobHandle) {
        // TODO no integration with jobs yet
    }

    @Override
    public void jobRemoved(JobRemoved jobRemoved) {
        // TODO no integration with jobs yet
    }

    @Override
    public void jobChanged(JobProgress jobProgress) {
        // TODO no integration with jobs yet
    }
    @Override
    public CompletableFuture<String> promptString(StringPrompt stringPrompt) {
        return null;
    }

    @Override
    public void messageBox(MessageBoxNotification messageBoxNotification) {

    }

    @Override
    public void discoveryPathAdded(DiscoveryPath discoveryPath) {
        // Ignore, not worth showing / displaying
    }

    @Override
    public void discoveryPathRemoved(DiscoveryPath discoveryPath) {
        // Ignore, not worth showing / displaying
    }

    @Override
    public void serverAdded(ServerHandle serverHandle) {
        refreshView();
    }

    @Override
    public void serverRemoved(ServerHandle serverHandle) {
        refreshView();
    }

    @Override
    public void serverAttributesChanged(ServerHandle serverHandle) {
        refreshView();
    }

    @Override
    public void serverStateChanged(ServerState serverState) {
        refreshView();
    }

    @Override
    public void serverProcessCreated(ServerProcess serverProcess) {
        // Ignore, not worth showing / displaying
    }

    @Override
    public void serverProcessTerminated(ServerProcess serverProcess) {
        // Ignore, not worth showing / displaying
    }

    @Override
    public void serverProcessOutputAppended(ServerProcessOutput serverProcessOutput) {
        sendServerOutputToConsole(serverProcessOutput);
    }

}
