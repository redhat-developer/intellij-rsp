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
import org.jboss.tools.intellij.rsp.types.CommunityServerConnector;
import org.jboss.tools.intellij.rsp.types.RedHatServerConnector;
import org.jboss.tools.rsp.api.ICapabilityKeys;
import org.jboss.tools.rsp.api.dao.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class RspCore implements IRspCore {
    private static RspCore instance = new RspCore();
    public static RspCore getDefault() {
        return instance;
    }


    private Map<String,SingleRspModel> allRsps = new HashMap<>();
    private ArrayList<IRspCoreChangeListener> listeners = new ArrayList<>();
    private RspCore() {
        loadRSPs();
    }

    @Override
    public IRspType findServerType(String id) {
        SingleRspModel srm = allRsps.get(id);
        if( srm != null )
            return srm.getType();
        return null;
    }

    private SingleRspModel findModel(String typeId) {
        return allRsps.get(typeId);
    }

    private void loadRSPs() {
        // TODO load from xml file or something
        IRsp rht = new RedHatServerConnector().getRsp(this);
        IRsp community = new CommunityServerConnector().getRsp(this);
        allRsps.put(rht.getServerType().getId(), new SingleRspModel(rht));
        allRsps.put(community.getServerType().getId(), new SingleRspModel(community));
    }

    public void startServer(IRsp server) {
        ServerConnectionInfo info = server.start();
        if( info != null ) {
            try {
                IntelliJRspClientLauncher launcher = launch(server, info.getHost(), info.getPort());
                String typeId = server.getServerType().getId();
                SingleRspModel srm = findModel(typeId);
                if( srm != null ) {
                    srm.setClient(launcher);
                }
            } catch(IOException e ) {

            } catch(InterruptedException ie) {

            } catch( ExecutionException ee) {

            }
        }
    }

    public IntelliJRspClientLauncher getClient(IRsp rsp) {
        SingleRspModel srm = findModel(rsp.getServerType().getId());
        return srm == null ? null : srm.getClient();
    }

    @Override
    public void stopServer(IRsp server) {
        server.stop();
    }

    @Override
    public void stateUpdated(RspImpl rspServer) {
        if( rspServer.getState() == IJServerState.STOPPED) {
            SingleRspModel srm = findModel(rspServer.getServerType().getId());
            if(srm != null ) {
                srm.setClient(null);
                srm.clear();
            }
        }
        modelUpdated(rspServer);
    }

    private IntelliJRspClientLauncher launch(IRsp rsp, String host, int port) throws IOException, InterruptedException, ExecutionException {
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


    public IRsp[] getRSPs() {
        List<IRsp> ret = allRsps.values().stream().filter(p->p.getServer() != null).
                map(SingleRspModel::getServer).collect(Collectors.toList());

        return ret.toArray(new IRsp[ret.size()]);
    };

    public ServerState[] getServersInRsp(IRsp rsp) {
        SingleRspModel srm = findModel(rsp.getServerType().getId());
        List<ServerState> states = srm.getServerState();
        return states.toArray(new ServerState[states.size()]);
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



    /*
    Events from clients
     */
    @Override
    public void jobAdded(IRsp rsp, JobHandle jobHandle) {
        SingleRspModel model = findModel(rsp.getServerType().getId());
        if( model != null ) {
            model.addJob(jobHandle);
            modelUpdated(rsp);
        }
    }

    @Override
    public void jobRemoved(IRsp rsp, JobRemoved jobRemoved) {
        SingleRspModel model = findModel(rsp.getServerType().getId());
        if( model != null ) {
            model.removeJob(jobRemoved.getHandle());
            modelUpdated(rsp);
        }
    }

    @Override
    public void jobChanged(IRsp rsp, JobProgress jobProgress) {
        SingleRspModel model = findModel(rsp.getServerType().getId());
        if( model != null ) {
            model.jobChanged(jobProgress);
            modelUpdated(rsp);
        }
    }
    @Override
    public void serverAdded(IRsp rsp, ServerHandle serverHandle) {
        SingleRspModel model = findModel(rsp.getServerType().getId());
        if( model != null ) {
            model.addServer(serverHandle);
            modelUpdated(rsp);
        }
    }

    @Override
    public void serverRemoved(IRsp rsp, ServerHandle serverHandle) {
        SingleRspModel model = findModel(rsp.getServerType().getId());
        if( model != null )
            model.removeServer(serverHandle);
    }

    @Override
    public void serverAttributesChanged(IRsp rsp, ServerHandle serverHandle) {
        // Ignore?
    }

    @Override
    public void serverStateChanged(IRsp rsp, ServerState serverState) {
        SingleRspModel model = findModel(rsp.getServerType().getId());
        if( model != null ) {
            model.updateServer(serverState);
            modelUpdated(rsp);
        }
    }
    @Override
    public CompletableFuture<String> promptString(IRsp rsp, StringPrompt stringPrompt) {
        return null; // TODO
    }

    @Override
    public void messageBox(IRsp rsp, MessageBoxNotification messageBoxNotification) {
    }


}
