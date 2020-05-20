package org.jboss.tools.intellij.rsp.types;

import com.intellij.openapi.util.IconLoader;
import org.jboss.tools.intellij.rsp.model.*;
import org.jboss.tools.intellij.rsp.model.impl.ReferenceRspControllerImpl;
import org.jboss.tools.intellij.rsp.model.impl.RspTypeImpl;

import javax.swing.*;

public class CommunityServerConnector extends AbstractServerConnector {
    public CommunityServerConnector() {
        super("redhat-community-server-connector", "Community Server Connector by Red Hat",
                9000, 9500, "images/storage.png");
    }
    public IRsp getRsp(IRspCore core) {
        String version = "0.22.10";
        String url = "https://download.jboss.org/jbosstools/adapters/stable/rsp-server-community/distributions/0.22.10.Final/org.jboss.tools.rsp.server.community.distribution-0.22.10.Final.zip";
        return getType(core).createRsp(version, url);
    }

    @Override
    protected Icon findIconForServerType(String serverType) {
        return null;
    }

}
