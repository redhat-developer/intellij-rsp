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
import java.io.File;

public class RspTypeImpl implements IRspType {

    public static final String SYSPROP_USER_HOME = "user.home";
    public static final String DATA_LOCATION_DEFAULT = ".rsp";
    public static final String INSTALLATIONS = ".rspInstalls";
    public static final String EXPANDED = "expanded";
    public static final String DOWNLOADS = "downloads";

    public static final String FILE_DOT_VERSION = ".distribution.version";


    private final IServerIconProvider iconProvider;
    private final String name;
    private final String id;
    private final IRspCore model;
    private IRspStateControllerProvider controllerProvider;

    public RspTypeImpl(IRspCore model, String id,
                       String name,
                       IServerIconProvider iconProvider,
                       IRspStateControllerProvider controllerProvider) {
        this.model = model;
        this.id = id;
        this.name = name;
        this.iconProvider = iconProvider;
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
        return iconProvider.getIcon();
    }

    @Override
    public Icon getIcon(String serverTypeId) {
        return iconProvider.getIcon(serverTypeId);
    }

    public static File getServerTypeInstallLocation(IRspType type) {
        File home = new File(System.getProperty(SYSPROP_USER_HOME));
        File root = new File(home, DATA_LOCATION_DEFAULT);
        File installs = new File(root, INSTALLATIONS);
        File expanded = new File(installs, EXPANDED);
        File unzipLoc = new File(expanded, type.getId());
        return unzipLoc;
    }

    @Override
    public String getServerHome() {
        File unzipLoc = getServerTypeInstallLocation(this);
        if( unzipLoc.exists() && unzipLoc.listFiles().length == 1 && unzipLoc.listFiles()[0].isDirectory()) {
            return unzipLoc.listFiles()[0].getAbsolutePath();
        }
        return unzipLoc.getAbsolutePath();
    }

    @Override
    public IRsp createRsp(String version, String url) {
        return new RspImpl(model,this, version, url, createController());
    }

    protected IRspStateController createController() {
        return controllerProvider.createController(this);
    }
}
