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
package org.jboss.tools.intellij.rsp.model;

import org.jboss.tools.intellij.rsp.client.IntelliJRspClientLauncher;
import org.jboss.tools.intellij.rsp.model.impl.RspImpl;
import org.jboss.tools.intellij.rsp.model.impl.SingleRspModel;
import org.jboss.tools.rsp.api.dao.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Represents the primary model for the UI tooling
 */
public interface IRspCore {

    /*
    Events from clients
     */
    void jobAdded(IRsp rsp, JobHandle jobHandle);

    void jobRemoved(IRsp rsp, JobRemoved jobRemoved);

    void jobChanged(IRsp rsp, JobProgress jobProgress);

    CompletableFuture<String> promptString(IRsp rsp, StringPrompt stringPrompt);

    void messageBox(IRsp rsp, MessageBoxNotification messageBoxNotification);

    void serverAdded(IRsp rsp, ServerHandle serverHandle);

    void serverRemoved(IRsp rsp, ServerHandle serverHandle);

    void serverAttributesChanged(IRsp rsp, ServerHandle serverHandle);

    void serverStateChanged(IRsp rsp, ServerState serverState);

    void serverProcessCreated(IRsp rsp, ServerProcess serverProcess);
    void serverProcessTerminated(IRsp rsp, ServerProcess serverProcess);
    void serverProcessOutputAppended(IRsp rsp, ServerProcessOutput serverProcessOutput);


    public enum IJServerState {
        STOPPING,
        STOPPED,
        STARTING,
        STARTED
    }

    public void startServer(IRsp server);
    public void stopServer(IRsp server);
    public void stateUpdated(RspImpl rspServer);

    /**
     * Some element in the tree has been updated.
     * May be null. If null, update the root element
     * @param o
     */
    public void modelUpdated(Object o);

    /**
     * Add a listener to respond to model changes
     * @param listener
     */
    public void addChangeListener(IRspCoreChangeListener listener);
    /**
     * Remove a listener to respond to model changes
     * @param listener
     */
    public void removeChangeListener(IRspCoreChangeListener listener);

    /**
     * Get a list of all declared RSPs
     * @return
     */
    public IRsp[] getRSPs();

    /**
     * Find an RSP type by the given id
     * @param id
     * @return
     */
    public IRspType findRspType(String id);

    /**
     * Get a list of servers defined inside a given rsp
     * @param rsp
     * @return
     */
    public ServerState[] getServersInRsp(IRsp rsp);

    /**
     * Get all currently-running jobs for the given RSP
     * @param rsp
     * @return
     */
    public JobProgress[] getJobs(IRsp rsp);

    /**
     * Get a client for the remote RSP which can make requests
     * @param rsp
     * @return
     */
    public IntelliJRspClientLauncher getClient(IRsp rsp);
}
