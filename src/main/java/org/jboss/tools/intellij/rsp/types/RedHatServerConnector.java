package org.jboss.tools.intellij.rsp.types;

import com.intellij.openapi.util.IconLoader;
import org.jboss.tools.intellij.rsp.model.IRsp;
import org.jboss.tools.intellij.rsp.model.IRspCore;
import org.jboss.tools.intellij.rsp.model.IRspType;
import org.jboss.tools.intellij.rsp.model.impl.RspTypeImpl;

import javax.swing.*;

public class RedHatServerConnector extends AbstractServerConnector {
    public RedHatServerConnector() {
        super("redhat-server-connector", "Red Hat Server Connector",
                8500, 8999, "images/jboss.eap-24x24.png");
    }
    public IRsp getRsp(IRspCore core) {
        String version = "0.22.9";
        String url = "https://download.jboss.org/jbosstools/adapters/snapshots/rsp-server/org.jboss.tools.rsp.distribution.wildfly-0.22.9.Final.zip";
        return getType(core).createRsp(version, url);
    }

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
            return "jboss.png";
        } else if (serverType.startsWith("org.jboss.ide.eclipse.as.")) {
            return "jboss.png";
        } else {
            return "jboss.eap-24x24.png";
        }
    }

}
