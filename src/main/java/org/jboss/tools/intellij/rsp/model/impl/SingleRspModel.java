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
import org.jboss.tools.intellij.rsp.model.IRsp;
import org.jboss.tools.intellij.rsp.model.IRspType;
import org.jboss.tools.intellij.rsp.util.RemoteServerProcess;
import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The model for a single RSP, including its type, client, jobs currently executing,
 * and state for each of the servers declared on the RSP (including deployment state)
 */
public class SingleRspModel {
    private IRspType type;
    private IRsp server;
    private IntelliJRspClientLauncher client;
    private List<JobProgress> jobs;
    private List<ServerState> serverState;

    private HashMap<String, RemoteServerProcess> processes;

    public SingleRspModel(IRsp server) {
        this.server = server;
        this.type = server.getRspType();
        this.jobs = new ArrayList<>();
        this.serverState = new ArrayList<>();
        this.processes = new HashMap<>();
    }

    public void setClient(IntelliJRspClientLauncher client) {
        this.client = client;
    }

    public IRspType getType() {
        return type;
    }
    public IRsp getServer() {
        return server;
    }
    public IntelliJRspClientLauncher getClient() {
        return client;
    }
    public List<JobProgress> getJobs() {
        return new ArrayList<>(jobs);
    }
    public List<ServerState> getServerState() {
        return serverState;
    }

    public void addJob(JobHandle jobHandle) {
        JobProgress jp = new JobProgress(jobHandle, 0);
        jobs.add(jp);
    }
    public void removeJob(JobHandle jobHandle) {
        for( JobProgress jp : new ArrayList<>(jobs) ) {
            if( jp.getHandle().getId().equals(jobHandle.getId()) && jp.getHandle().getName().equals(jobHandle.getName())) {
                jobs.remove(jp);
            }
        }
    }

    public void jobChanged(JobProgress jobProgress) {
        JobHandle jobHandle = jobProgress.getHandle();
        for( JobProgress jp : new ArrayList<>(jobs) ) {
            if( jp.getHandle().getId().equals(jobHandle.getId()) && jp.getHandle().getName().equals(jobHandle.getName())) {
                jp.setPercent(jobProgress.getPercent());
            }
        }
    }

    public void addServer(ServerHandle serverHandle) {
        if( findServerState(serverHandle) == null) {
            ServerState ss = new ServerState();
            ss.setServer(serverHandle);
            ss.setState(ServerManagementAPIConstants.STATE_UNKNOWN);
            ss.setPublishState(ServerManagementAPIConstants.PUBLISH_STATE_UNKNOWN);
            serverState.add(ss);
        }
    }
    public void removeServer(ServerHandle serverHandle) {
        ServerState ss = findServerState(serverHandle);
        if( ss != null ) {
            serverState.remove(ss);
        }
    }
    public void updateServer(ServerState state) {
        ServerState ss = findServerState(state.getServer());
        if( ss != null ) {
            ss.setServer(state.getServer());
            ss.setDeployableStates(state.getDeployableStates());
            ss.setPublishState(state.getPublishState());
            ss.setState(state.getState());
            ss.setRunMode(state.getRunMode());
        } else {
            serverState.add(state);
        }
    }

    private ServerState findServerState(ServerHandle serverHandle) {
        for( ServerState ss : new ArrayList<>(serverState)) {
            ServerHandle ssh = ss.getServer();
            if( ssh.getId().equals(serverHandle.getId()) && ssh.getType().getId().equals(serverHandle.getType().getId()))
                return ss;
        }
        return null;
    }

    public void clear() {
        this.serverState = new ArrayList<>();
        this.jobs = new ArrayList<>();
    }

    public Process addServerProcess(ServerProcess serverProcess) {
        String id = serverProcess.getServer().toString() + ":" + serverProcess.getProcessId();
        RemoteServerProcess sp = new RemoteServerProcess();
        processes.put(id, sp);
        return sp;
    }

    public void serverProcessTerminated(ServerProcess serverProcess) {
        String id = serverProcess.getServer().toString() + ":" + serverProcess.getProcessId();
        RemoteServerProcess sp = processes.get(id);
        if( sp != null ) {
            sp.terminate();;
            processes.remove(id);
        }
    }

    public void serverProcessOutputAppended(ServerProcessOutput serverProcessOutput) {
        String id = serverProcessOutput.getServer().toString() + ":" + serverProcessOutput.getProcessId();
        RemoteServerProcess sp = processes.get(id);
        if( sp != null ) {
            sp.handleEvent(serverProcessOutput);
        }
    }
}