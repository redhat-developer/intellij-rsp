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

import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.rsp.model.*;
import com.redhat.devtools.intellij.rsp.types.CommunityServerConnector;
import com.redhat.devtools.intellij.rsp.ui.dialogs.StringPromptDialog;
import com.redhat.devtools.intellij.rsp.client.IntelliJRspClientLauncher;
import com.redhat.devtools.intellij.rsp.types.RedHatServerConnector;
import com.redhat.devtools.intellij.rsp.ui.util.UIHelper;
import com.redhat.devtools.intellij.rsp.util.ExecUtilClone;
import com.redhat.devtools.intellij.rsp.util.RspProcessHandler;
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

/**
 * The primary model for the framework. It mostly just links events from rsp and client
 * to listeners and stores some basic information about each model object
 */
public class RspCore implements IRspCore {
    private static RspCore instance = new RspCore();
    public static RspCore getDefault() {
        return instance;
    }


    private Map<String,SingleRspModel> allRsps = new HashMap<>();
    private List<IRspCoreChangeListener> listeners = new ArrayList<>();
    private Map<String, RspProgressJob> uiJobs = new HashMap<>();

    private RspCore() {
        loadRSPs();
    }

    @Override
    public IRspType findRspType(String id) {
        SingleRspModel srm = allRsps.get(id);
        if( srm != null )
            return srm.getType();
        return null;
    }

    @Override
    public IRsp findRsp(String id) {
        SingleRspModel srm = allRsps.get(id);
        if( srm != null )
            return srm.getServer();
        return null;
    }

    private SingleRspModel findModel(String typeId) {
        return allRsps.get(typeId);
    }

    private void loadRSPs() {
        // TODO load from xml file or something
        IRsp rht = new RedHatServerConnector().getRsp(this);
        IRsp community = new CommunityServerConnector().getRsp(this);
        allRsps.put(rht.getRspType().getId(), new SingleRspModel(rht));
        allRsps.put(community.getRspType().getId(), new SingleRspModel(community));
    }

    public void startServer(IRsp server) {
        ServerConnectionInfo info = server.start();
        if( info != null ) {
            try {
                IntelliJRspClientLauncher launcher = launch(server, info.getHost(), info.getPort());
                String typeId = server.getRspType().getId();
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
        SingleRspModel srm = findModel(rsp.getRspType().getId());
        return srm == null ? null : srm.getClient();
    }

    @Override
    public void stopServer(IRsp server) {
        server.stop();
    }

    @Override
    public void stateUpdated(RspImpl rspServer) {
        if( rspServer.getState() == IJServerState.STOPPED) {
            SingleRspModel srm = findModel(rspServer.getRspType().getId());
            if(srm != null ) {
                srm.setClient(null);
                srm.clear();
            }
        }
        modelUpdated(rspServer);
    }

    private IntelliJRspClientLauncher launch(IRsp rsp, String host, int port) throws IOException, InterruptedException, ExecutionException {
        IntelliJRspClientLauncher launcher = new IntelliJRspClientLauncher(rsp, host, port);
        launcher.setListener(() -> {
            rsp.terminate();
        });
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


    @Override
    public IRsp[] getRSPs() {
        List<IRsp> ret = allRsps.values().stream().filter(p->p.getServer() != null).
                map(SingleRspModel::getServer).collect(Collectors.toList());

        return ret.toArray(new IRsp[ret.size()]);
    };

    @Override
    public ServerState[] getServersInRsp(IRsp rsp) {
        SingleRspModel srm = findModel(rsp.getRspType().getId());
        List<ServerState> states = srm.getServerState();
        return states.toArray(new ServerState[states.size()]);
    }

    @Override
    public ServerState findServerInRsp(IRsp rsp, String serverId) {
        ServerState[] all = getServersInRsp(rsp);
        if( all == null )
            return null;
        for( int i = 0; i < all.length; i++ ) {
            if( all[i].getServer().getId().equals(serverId))
                return all[i];
        }
        return null;
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
        SingleRspModel model = findModel(rsp.getRspType().getId());
        if( model != null ) {
            model.addJob(jobHandle);
            String id = jobHandleToUniqueId(rsp, jobHandle);
            RspProgressJob progJob = new RspProgressJob(rsp, jobHandle);
            uiJobs.put(id, progJob);
            ProgressManager.getInstance().run(progJob);
            modelUpdated(rsp);
        }
    }

    private String jobHandleToUniqueId(IRsp rsp, JobHandle handle) {
        return rsp.getRspType().getId() + ":" + handle.getId();
    }

    @Override
    public void jobRemoved(IRsp rsp, JobRemoved jobRemoved) {
        SingleRspModel model = findModel(rsp.getRspType().getId());
        if( model != null ) {
            model.removeJob(jobRemoved.getHandle());
            String id = jobHandleToUniqueId(rsp, jobRemoved.getHandle());
            RspProgressJob uiJob = uiJobs.get(id);
            if( uiJob != null ) {
                uiJob.setJobRemoved(jobRemoved);
                uiJobs.remove(id);
            }
            modelUpdated(rsp);
        }
    }

    @Override
    public void jobChanged(IRsp rsp, JobProgress jobProgress) {

        SingleRspModel model = findModel(rsp.getRspType().getId());
        if( model != null ) {
            model.jobChanged(jobProgress);
            String id = jobHandleToUniqueId(rsp, jobProgress.getHandle());
            RspProgressJob uiJob = uiJobs.get(id);
            if( uiJob != null ) {
                uiJob.setJobProgress(jobProgress);
            }
            modelUpdated(rsp);
        }
    }

    @Override
    public JobProgress[] getJobs(IRsp rsp) {
        SingleRspModel model = findModel(rsp.getRspType().getId());
        if( model != null ) {
            List<JobProgress> jps = model.getJobs();
            return jps.toArray(new JobProgress[0]);
        }
        return new JobProgress[0];
    }

    @Override
    public void serverAdded(IRsp rsp, ServerHandle serverHandle) {
        SingleRspModel model = findModel(rsp.getRspType().getId());
        if( model != null ) {
            model.addServer(serverHandle);
            modelUpdated(rsp);
        }
    }

    @Override
    public void serverRemoved(IRsp rsp, ServerHandle serverHandle) {
        SingleRspModel model = findModel(rsp.getRspType().getId());
        if( model != null ) {
            model.removeServer(serverHandle);
            modelUpdated(rsp);
        }
    }

    @Override
    public void serverAttributesChanged(IRsp rsp, ServerHandle serverHandle) {
        // Ignore?
    }

    @Override
    public void serverStateChanged(IRsp rsp, ServerState serverState) {
        SingleRspModel model = findModel(rsp.getRspType().getId());
        if( model != null ) {
            model.updateServer(serverState);
            modelUpdated(rsp);
        }
    }

    @Override
    public void serverProcessCreated(IRsp rsp, ServerProcess serverProcess) {
        SingleRspModel model = findModel(rsp.getRspType().getId());
        if( model != null ) {
            Process p = model.addServerProcess(serverProcess);
            String name = serverProcess.getServer().getId() + ":" + serverProcess.getProcessId();
            Project project = ProjectManager.getInstance().getOpenProjects()[0];

            try {
                ExecHelper.linkProcessToTerminal(new RspProcessHandler(p,name, "run server " + name), project, name,false);
            } catch(IOException ioe) {
                // TODO cleanup
                ioe.printStackTrace();
            }
        }
    }

    @Override
    public void serverProcessTerminated(IRsp rsp, ServerProcess serverProcess) {
        SingleRspModel model = findModel(rsp.getRspType().getId());
        if( model != null ) {
            model.serverProcessTerminated(serverProcess);
        }
    }

    @Override
    public void serverProcessOutputAppended(IRsp rsp, ServerProcessOutput serverProcessOutput) {
        SingleRspModel model = findModel(rsp.getRspType().getId());
        if( model != null ) {
            model.serverProcessOutputAppended(serverProcessOutput);
        }
    }

    @Override
    public CompletableFuture<String> promptString(IRsp rsp, StringPrompt stringPrompt) {
        final String[] ret = new String[1];
        UIHelper.executeInUI(() -> {
            StringPromptDialog spd = new StringPromptDialog(rsp, stringPrompt);
            spd.show();
            if( spd.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
                ret[0] = spd.getText();
            } else {
                ret[0] = null;
            }
        });
        return CompletableFuture.completedFuture(ret[0]);
    }

    @Override
    public void messageBox(IRsp rsp, MessageBoxNotification messageBoxNotification) {
        int sev = messageBoxNotification.getSeverity();
        if( sev == Status.ERROR) {
            Messages.showErrorDialog(messageBoxNotification.getMessage(), "Error");
        } else if( sev == Status.INFO) {
            Messages.showInfoMessage(messageBoxNotification.getMessage(), "Info");
        } else if( sev == Status.WARNING) {
            Messages.showWarningDialog(messageBoxNotification.getMessage(), "Warning");
        } else if( sev == Status.OK) {
            Messages.showInfoMessage(messageBoxNotification.getMessage(), "Info");
        }
    }


}
