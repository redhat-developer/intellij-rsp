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

    public void addChangeListener(IRspCoreChangeListener listener);
    public void removeChangeListener(IRspCoreChangeListener listener);

    public IRsp[] getRSPs();
    public IRspType findRspType(String id);
    public ServerState[] getServersInRsp(IRsp rsp);
    public JobProgress[] getJobs(IRsp rsp);
    public IntelliJRspClientLauncher getClient(IRsp rsp);
}
