package org.jboss.tools.intellij.rsp.model;

import javax.swing.*;

public interface IServerIconProvider {
    public Icon getIcon();
    public Icon getIcon(String serverType);
}
