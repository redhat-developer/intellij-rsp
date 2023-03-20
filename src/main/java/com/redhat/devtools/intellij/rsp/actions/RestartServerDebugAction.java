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
package com.redhat.devtools.intellij.rsp.actions;

import com.intellij.openapi.project.Project;
import com.redhat.devtools.intellij.rsp.telemetry.TelemetryService;
import com.redhat.devtools.intellij.rsp.ui.tree.RspTreeModel;
import com.redhat.devtools.intellij.rsp.client.IntelliJRspClientLauncher;

public class RestartServerDebugAction extends RestartServerAction {
    protected void startServer(RspTreeModel.ServerStateWrapper sel, Project project, IntelliJRspClientLauncher client) {
        StartServerDebugAction.startServerDebugModeInternal(sel, project, client);
    }
    protected void telemActionCalled(RspTreeModel.ServerStateWrapper sel) {
        String typeId = sel.getServerState().getServer().getType().getId();
        String[] keys = new String[]{"mode"};
        String[] vals = new String[]{"debug"};
        TelemetryService.instance().sendWithType(TelemetryService.TELEMETRY_SERVER_RESTART, typeId, null, null, keys, vals);
    }

}
