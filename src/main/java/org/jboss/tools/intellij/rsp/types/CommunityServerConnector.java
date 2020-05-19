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
        return getType(core).createRsp("0.22.10", "/home/rob/path/to/something");
    }

    @Override
    protected Icon findIconForServerType(String serverType) {
        return null;
    }

}
