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
import org.jboss.tools.rsp.api.ICapabilityKeys;
import org.jboss.tools.rsp.api.dao.ClientCapabilitiesRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class RspCore implements IRspCore {
    private static RspCore instance = new RspCore();
    public static RspCore getDefault() {
        return instance;
    }

    private ArrayList<IRspServer> rsps = new ArrayList<>();
    private ArrayList<IRspServerType> type = new ArrayList<>();
    private Map<String, IntelliJRspClientLauncher> rspToClient = new HashMap<>();

    private ArrayList<IRspCoreChangeListener> listeners = new ArrayList<>();


    private RspCore() {
        loadRSPs();
    }

    private IRspServerType addServerType(String id, String name, String icon, IRspStateControllerProvider controller) {
        RspServerTypeImpl ret = (new RspServerTypeImpl(this,id, name, icon, controller));
        type.add(ret);
        return ret;
    }

    @Override
    public IRspServerType findServerType(String id) {
        for( IRspServerType r : type ) {
            if( r.getId().equals(id))
                return r;
        }
        return null;
    }

    private IRspStateControllerProvider createReferenceControllerProvider(final int portMin, final int portMax) {
        return new IRspStateControllerProvider() {
            @Override
            public IRspStateController createController(IRspServerType rspServerType, String version, String home) {
                return new ReferenceRspControllerImpl(rspServerType, version, home, portMin, portMax);
            }
        };
    }

    private void loadRSPs() {
        IRspServerType rht = addServerType("redhat-server-connector", "Red Hat Server Connector", "images/service.png",
                createReferenceControllerProvider(8500,8999));
        IRspServerType community = addServerType("redhat-community-server-connector", "Community Server Connector by Red Hat", "images/storage.png",
                createReferenceControllerProvider(9000,9500));

        // TODO load from xml file or something
        IRspServer rht1 = rht.createServer(this,"0.22.10", "/home/rob/code/work/rsp/rsp-server/distribution/distribution/target/rsp-distribution");
        IRspServer community1 = community.createServer(this,"0.22.10", "/home/rob/path/to/something");
        rsps.add(rht1);
        rsps.add(community1);

    }

    public void startServer(IRspServer server) {
        ServerConnectionInfo info = server.start();
        if( info != null ) {
            try {
                IntelliJRspClientLauncher launcher = launch(server, info.getHost(), info.getPort());
                String typeId = server.getServerType().getId();
                rspToClient.put(typeId, launcher);
            } catch(IOException e ) {

            } catch(InterruptedException ie) {

            } catch( ExecutionException ee) {

            }
        }
    }

    public IntelliJRspClientLauncher getClient(IRspServer rsp) {
        String id = rsp.getServerType().getId();
        return rspToClient.get(id);
    }

    @Override
    public void stopServer(IRspServer server) {
        server.stop();
    }

    @Override
    public void stateUpdated(RspServerImpl rspServer) {
        if( rspServer.getState() == IJServerState.STOPPED) {
            // cleanup
            rspToClient.remove(rspServer.getServerType().getId());
        }
        modelUpdated(rspServer);
        // TODO fire to listener model / view
    }

    private IntelliJRspClientLauncher launch(IRspServer rsp, String host, int port) throws IOException, InterruptedException, ExecutionException {
        IntelliJRspClientLauncher launcher = new IntelliJRspClientLauncher(rsp, host, port);
        //launcher.setListener(() -> {
            // TODO or do nothing / delete this block?
        //});
        launcher.launch();
        ClientCapabilitiesRequest clientCapRequest = createClientCapabilitiesRequest();
        launcher.getServerProxy().registerClientCapabilities(clientCapRequest).get();
        return launcher;
    }

    private ClientCapabilitiesRequest createClientCapabilitiesRequest() {
        Map<String, String> clientCap = new HashMap<>();
        clientCap.put(ICapabilityKeys.STRING_PROTOCOL_VERSION, ICapabilityKeys.PROTOCOL_VERSION_0_10_0);
        clientCap.put(ICapabilityKeys.BOOLEAN_STRING_PROMPT, Boolean.toString(true));
        return new ClientCapabilitiesRequest(clientCap);
    }


    public IRspServer[] getRSPs() {
        return rsps.toArray(new IRspServer[0]);
    };

    public String[] tmpGetChildren(IRspServer rsp) {
        return new String[] { "a", "b", "c"};
    }


    @Override
    public void modelUpdated(Object o) {
        for( IRspCoreChangeListener l : listeners ) {
            l.modelChanged(o);
        }
    }

    @Override
    public void addChangeListener(IRspCoreChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeChangeListener(IRspCoreChangeListener listener) {
        listeners.remove(listener);
    }

}
