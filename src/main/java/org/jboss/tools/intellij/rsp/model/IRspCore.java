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
import org.jboss.tools.intellij.rsp.model.impl.RspServerImpl;

public interface IRspCore {

    public enum IJServerState {
        STOPPING,
        STOPPED,
        STARTING,
        STARTED
    }

    public IRspServer[] getRSPs();
    public IRspServerType findServerType(String id);
    public void startServer(IRspServer server);
    public void stopServer(IRspServer server);
    public void stateUpdated(RspServerImpl rspServer);

    /**
     * Some element in the tree has been updated.
     * May be null. If null, update the root element
     * @param o
     */
    public void modelUpdated(Object o);

    public void addChangeListener(IRspCoreChangeListener listener);
    public void removeChangeListener(IRspCoreChangeListener listener);

    public IntelliJRspClientLauncher getClient(IRspServer rsp);
}
