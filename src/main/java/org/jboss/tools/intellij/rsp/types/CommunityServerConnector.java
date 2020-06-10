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
import org.jboss.tools.intellij.rsp.model.IRsp;
import org.jboss.tools.intellij.rsp.model.IRspCore;

import javax.swing.*;

public class CommunityServerConnector extends AbstractServerConnector {
    public CommunityServerConnector() {
        super("redhat-community-server-connector", "Community Server Connector by Red Hat",
                9000, 9500, "images/community-12x24.png");
    }
    public IRsp getRsp(IRspCore core) {
        String version = "0.22.10";
        String url = "https://download.jboss.org/jbosstools/adapters/stable/rsp-server-community/distributions/0.22.10.Final/org.jboss.tools.rsp.server.community.distribution-0.22.10.Final.zip";
        return getType(core).createRsp(version, url);
    }

    @Override
    protected Icon findIconForServerType(String serverType) {
        String path = getIconFileForServerType(serverType);
        if( path == null )
            return null;
        return IconLoader.getIcon("images/" + getIconFileForServerType(serverType));
    }

    private String getIconFileForServerType(String serverType) {
        if( serverType == null ) {
            return "server-light-24x24.png";
        }

        if( serverType.toLowerCase().indexOf("karaf") != -1) {
            return "karaf-24x24.png";
        }
        if( serverType.toLowerCase().indexOf("tomcat") != -1) {
            return "tomcat-24x24.png";
        }
        if( serverType.toLowerCase().indexOf("felix") != -1 ) {
            return "felix-24x24.png";
        }
        if( serverType.toLowerCase().indexOf("jetty") != -1 ) {
            return "jetty-24x24.png";
        }
        if( serverType.toLowerCase().indexOf("glassfish") != -1 ) {
            return "glassfish-24x24.png";
        }
        return "server-dark-24x24.png";
    }

}
