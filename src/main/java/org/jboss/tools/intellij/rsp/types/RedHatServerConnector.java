package org.jboss.tools.intellij.rsp.types;

import org.jboss.tools.intellij.rsp.model.IRsp;
import org.jboss.tools.intellij.rsp.model.IRspCore;
import org.jboss.tools.intellij.rsp.model.IRspType;
import org.jboss.tools.intellij.rsp.model.impl.RspTypeImpl;

import javax.swing.*;

public class RedHatServerConnector extends AbstractServerConnector {
    public RedHatServerConnector() {
        super("redhat-server-connector", "Red Hat Server Connector",
                8500, 8999, "images/storage.png");
    }
    public IRsp getRsp(IRspCore core) {
        String version = "0.22.9";
        String url = "https://download.jboss.org/jbosstools/adapters/snapshots/rsp-server/org.jboss.tools.rsp.distribution.wildfly-0.22.9.Final.zip";
        return getType(core).createRsp(version, url);
    }

    @Override
    protected Icon findIconForServerType(String serverType) {
        return null;
    }

}
