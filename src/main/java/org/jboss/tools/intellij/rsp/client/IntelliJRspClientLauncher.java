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
import org.jboss.tools.rsp.api.RSPServer;
import org.jboss.tools.rsp.api.SocketLauncher;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class IntelliJRspClientLauncher {
    private IntelliJServerManagementClientImpl myClient;
    private SocketLauncher<RSPServer> launcher;
    private Socket socket;
    private String host;
    private int port;
    private boolean connectionOpen = false;
    private IClientConnectionClosedListener listener;
    private IRspServer rsp;
    public IntelliJRspClientLauncher(IRspServer rsp, String host, int port) {
        this.host = host;
        this.port = port;
        this.rsp = rsp;
    }

    public void launch() throws UnknownHostException, IOException {
        // create the chat client
        IntelliJServerManagementClientImpl client = new IntelliJServerManagementClientImpl(rsp);
        // connect to the server
        this.socket = new Socket(host, port);
        // open a JSON-RPC connection for the opened socket
        this.launcher = new SocketLauncher<>(client, RSPServer.class, socket);
        //
        // Start listening for incoming message.
        // When the JSON-RPC connection is closed,
        // e.g. the server is died,
        // the client process should exit.
        launcher.startListening().thenRun(() -> clientClosed());
        // start the chat session with a remote chat server proxy
        client.initialize(launcher.getRemoteProxy());
        myClient = client;
        connectionOpen = true;
    }

    private void clientClosed() {
        this.myClient = null;
        connectionOpen = false;
        if( listener != null )
            listener.connectionClosed();
    }

    public void closeConnection() {
        if( launcher != null ) {
            launcher.close();
        }
    }

    public IntelliJServerManagementClientImpl getClient() {
        return this.myClient;
    }

    public boolean isConnectionActive() {
        return connectionOpen;
    }

    public RSPServer getServerProxy() {
        if( myClient != null ) {
            return myClient.getProxy();
        }
        return null;
    }

    public void setListener(IClientConnectionClosedListener listener) {
        this.listener = listener;
    }

}
