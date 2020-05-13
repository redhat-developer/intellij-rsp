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

import com.intellij.openapi.util.IconLoader;
import org.jboss.tools.intellij.rsp.model.*;

import javax.swing.*;

public class RspServerTypeImpl implements IRspServerType {
    private final String iconLoc;
    private final String name;
    private final String id;
    private final IRspCore model;
    private IRspStateControllerProvider controllerProvider;

    public RspServerTypeImpl(IRspCore model, String id, String name, String iconLoc, IRspStateControllerProvider controllerProvider) {
        this.model = model;
        this.id = id;
        this.name = name;
        this.iconLoc = iconLoc;
        this.controllerProvider = controllerProvider;
    }
    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Icon getIcon() {
        return IconLoader.getIcon(iconLoc);
    }

    @Override
    public IRspServer createServer(IRspCore model, String version, String home) {
        return new RspServerImpl(model,this, version,home, createController(version, home));
    }

    private IRspStateController createController(String version, String home) {
        return controllerProvider.createController(this, version, home);
    }
}
