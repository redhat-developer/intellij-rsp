package org.jboss.tools.intellij.rsp.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jboss.tools.rsp.api.ServerManagementAPIConstants;

import javax.swing.tree.TreePath;

public class FullPublishServerAction extends IncrementalPublishServerAction {
    @Override
    protected void actionPerformed(AnActionEvent e, TreePath treePath, Object selected) {
        actionPerformedInternal(e, treePath, selected, ServerManagementAPIConstants.PUBLISH_FULL);
    }
}
