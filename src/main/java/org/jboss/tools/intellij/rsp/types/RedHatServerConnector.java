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

/**
 * Provides the id, name, ports, icons, and download information for
 * the latest version of the Red Hat Server Connector.
 */

public class RedHatServerConnector extends AbstractServerConnector {
    public RedHatServerConnector() {
        super("redhat-server-connector", "Red Hat Server Connector",
                8500, 8999, "images/jboss.eap-24x24.png",
                "https://download.jboss.org/jbosstools/adapters/stable/rsp-server/LATEST",
                "org.jboss.tools.rsp.distribution.latest.version", "org.jboss.tools.rsp.distribution.latest.url");
    }

    @Override
    protected IRsp createFallbackRsp(IRspCore core) {
        return getType(core).createRsp("0.23.8.Final",
                "https://download.jboss.org/jbosstools/adapters/stable/rsp-server/org.jboss.tools.rsp.distribution-0.23.8.Final.zip");
    };


    @Override
    protected Icon findIconForServerType(String serverType) {
        String path = getIconFileForServerType(serverType);
        if( path == null )
            return null;
        return IconLoader.getIcon("images/" + path);
    }

    private String getIconFileForServerType(String serverType) {
        if( serverType == null ) {
            return "jboss.eap-24x24.png";
        }

        if (serverType.startsWith("org.jboss.ide.eclipse.as.7")) {
            return "jbossas7_ligature-24x24.png";
        } else if (serverType.startsWith("org.jboss.ide.eclipse.as.runtime.7")) {
            return "jbossas7_ligature-24x24.png";
        } else if (serverType.startsWith("org.jboss.ide.eclipse.as.wildfly.")) {
            return "wildfly_icon-24x24.png";
        } else if (serverType.startsWith("org.jboss.ide.eclipse.as.runtime.wildfly.")) {
            return "wildfly_icon-24x24.png";
        } else if (serverType.startsWith("org.jboss.ide.eclipse.as.eap.")) {
            return "jboss.eap-24x24.png";
        } else if (serverType.startsWith("org.jboss.ide.eclipse.as.runtime.eap.")) {
            return "jboss.eap-24x24.png";
        } else if (serverType.startsWith("org.jboss.tools.openshift.cdk.server.type")) {
            return "openshift_extension-24x24.png";
        } else if (serverType.startsWith("org.jboss.ide.eclipse.as.runtime.")) {
            return "jboss-24x24.png";
        } else if (serverType.startsWith("org.jboss.ide.eclipse.as.")) {
            return "jboss-24x24.png";
        } else {
            return "jboss.eap-24x24.png";
        }
    }

}
