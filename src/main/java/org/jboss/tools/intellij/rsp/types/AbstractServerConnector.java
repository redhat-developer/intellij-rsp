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
package org.jboss.tools.intellij.rsp.types;

import com.intellij.openapi.util.IconLoader;
import org.jboss.tools.intellij.rsp.model.*;
import org.jboss.tools.intellij.rsp.model.impl.ReferenceRspControllerImpl;
import org.jboss.tools.intellij.rsp.model.impl.RspTypeImpl;

import javax.swing.*;

public abstract class AbstractServerConnector {
    protected String name;
    protected String id;
    protected int minPort;
    protected int maxPort;
    protected String iconPath;
    protected AbstractServerConnector(String id, String name,
                                      int minPort, int maxPort,
                                      String iconPath) {
        this.id = id;
        this.name = name;
        this.minPort = minPort;
        this.maxPort = maxPort;
        this.iconPath = iconPath;
    }

    public abstract IRsp getRsp(IRspCore core);

    protected IServerIconProvider createIconProvider() {
        return new IServerIconProvider() {
            @Override
            public Icon getIcon() {
                return IconLoader.getIcon(iconPath);
            }

            @Override
            public Icon getIcon(String serverType) {
                return findIconForServerType(serverType);
            }
        };
    }

    protected IRspType getType(IRspCore core) {
        return new RspTypeImpl(core,id, name,
                createIconProvider(),
                createReferenceControllerProvider(minPort, maxPort));
    }

    protected abstract Icon findIconForServerType(String serverType);

    protected IRspStateControllerProvider createReferenceControllerProvider(final int portMin, final int portMax) {
        return new IRspStateControllerProvider() {
            @Override
            public IRspStateController createController(IRspType rspServerType) {
                return new ReferenceRspControllerImpl(rspServerType, portMin, portMax);
            }
        };
    }


}
