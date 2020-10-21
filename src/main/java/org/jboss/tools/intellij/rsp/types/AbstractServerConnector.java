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
import org.apache.commons.compress.utils.IOUtils;
import org.jboss.tools.intellij.rsp.model.*;
import org.jboss.tools.intellij.rsp.model.impl.ReferenceRspControllerImpl;
import org.jboss.tools.intellij.rsp.model.impl.RspTypeImpl;

import javax.swing.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

/**
 * An abstract server connector for reference-implementation type RSPs
 * of the same type and structure as the redhat-server-connector or
 * community-server-connector
 */
public abstract class AbstractServerConnector {
    protected String name;
    protected String id;
    protected int minPort;
    protected int maxPort;
    protected String iconPath;
    protected String latestPropertiesUrl;
    protected String latestVersionKey;
    protected String latestUrlKey;
    protected AbstractServerConnector(String id, String name,
                                      int minPort, int maxPort,
                                      String iconPath,
                                      String latestPropertiesUrl,
                                      String latestVersionKey,
                                      String latestUrlKey) {
        this.id = id;
        this.name = name;
        this.minPort = minPort;
        this.maxPort = maxPort;
        this.iconPath = iconPath;
        this.latestPropertiesUrl = latestPropertiesUrl;
        this.latestVersionKey = latestVersionKey;
        this.latestUrlKey = latestUrlKey;
    }

    public IRsp getRsp(IRspCore core) {
        try {
            byte[] asBytes = downloadFile(latestPropertiesUrl);
            Properties props = new Properties();
            props.load(new ByteArrayInputStream(asBytes));
            String version = props.getProperty(latestVersionKey);
            String url = props.getProperty(latestUrlKey);
            return getType(core).createRsp(version, url);
        } catch(IOException ioe) {
            return createFallbackRsp(core);
        }
    }

    // Subclasses can override with a default hard-coded 'latest' as a fallback
    protected IRsp createFallbackRsp(IRspCore core) {
        return getType(core).createRsp();
    };

    public static byte[] downloadFile(String url) throws IOException  {
        URL url2 = new URL(url);
        URLConnection conn = url2.openConnection();
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.connect();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.copy(conn.getInputStream(), baos);

        return baos.toByteArray();
    }

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
