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
        return getType(core).createRsp("0.22.10", "/home/rob/code/work/rsp/rsp-server/distribution/distribution/target/rsp-distribution");
    }

    @Override
    protected Icon findIconForServerType(String serverType) {
        return null;
    }

}
