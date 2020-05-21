package org.jboss.tools.intellij.rsp.types;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.IconLoader;
import org.jboss.tools.intellij.rsp.model.*;
import org.jboss.tools.intellij.rsp.model.impl.ReferenceRspControllerImpl;
import org.jboss.tools.intellij.rsp.model.impl.RspTypeImpl;

import javax.swing.*;

public abstract class AbstractServerConnector {
    protected String name;
    protected String id;
    protected int minPort;
    protected int maxPort;
    protected String iconPath;
    protected AbstractServerConnector(String id, String name,
                                      int minPort, int maxPort,
                                      String iconPath) {
        this.id = id;
        this.name = name;
        this.minPort = minPort;
        this.maxPort = maxPort;
        this.iconPath = iconPath;
    }

    public abstract IRsp getRsp(IRspCore core);

    protected IServerIconProvider createIconProvider() {
        return new IServerIconProvider() {
            @Override
            public Icon getIcon() {
                return IconLoader.getIcon(iconPath);
            }

            @Override
            public Icon getIcon(String serverType) {
                Icon icon = findIconForServerType(serverType);
                if( icon == null ) {
                    icon = AllIcons.General.WarningDecorator;
                }
                return icon;
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
