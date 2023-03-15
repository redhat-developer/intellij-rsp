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

import com.intellij.execution.ProgramRunnerUtil;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.remote.RemoteConfiguration;
import com.intellij.execution.remote.RemoteConfigurationType;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ExecutionEnvironmentBuilder;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.redhat.devtools.intellij.rsp.ui.tree.RspTreeModel;
import com.redhat.devtools.intellij.rsp.util.PortFinder;
import com.redhat.devtools.intellij.rsp.client.IntelliJRspClientLauncher;
import com.redhat.devtools.intellij.rsp.model.impl.RspCore;
import com.redhat.devtools.intellij.rsp.ui.util.UIHelper;
import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.LaunchParameters;
import org.jboss.tools.rsp.api.dao.ServerAttributes;
import org.jboss.tools.rsp.api.dao.ServerHandle;
import org.jboss.tools.rsp.api.dao.StartServerResponse;

import javax.swing.tree.TreePath;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class StartServerDebugAction extends AbstractTreeAction {
    private static final String ERROR_STARTING_SERVER = "Error starting server";

    @Override
    protected boolean isVisible(Object[] o) {
        return safeSingleItemClass(o, RspTreeModel.ServerStateWrapper.class);
    }

    @Override
    protected boolean isEnabled(Object[] o) {
        if( o != null && o.length > 0 && o[0] instanceof RspTreeModel.ServerStateWrapper) {
            int state = ((RspTreeModel.ServerStateWrapper)o[0]).getServerState().getState();
            return state == ServerManagementAPIConstants.STATE_STOPPED || state == ServerManagementAPIConstants.STATE_UNKNOWN;
        }
        return false;
    }


    @Override
    protected void singleSelectionActionPerformed(AnActionEvent e, TreePath treePath, Object selected) {
        if (selected instanceof RspTreeModel.ServerStateWrapper) {
            final RspTreeModel.ServerStateWrapper sel = (RspTreeModel.ServerStateWrapper) selected;
            final Project project = ProjectManager.getInstance().getOpenProjects()[0];
            final IntelliJRspClientLauncher client = RspCore.getDefault().getClient(sel.getRsp());
            new Thread("Debugging server: " + sel.getServerState().getServer().getId()) {
                public void run() {
                    startServerDebugModeInternal(sel, project, client);
                }
            }.start();
        }
    }
    public static void startServerDebugModeInternal(RspTreeModel.ServerStateWrapper sel, Project project, IntelliJRspClientLauncher client) {
        String mode = "debug";
        ServerHandle handle = sel.getServerState().getServer();
        ServerAttributes sa = new ServerAttributes(handle.getType().getId(),
                sel.getServerState().getServer().getId(), new HashMap<String,Object>());
        LaunchParameters params = new LaunchParameters(sa, mode);
        final StartServerResponse response;
        try {
            response = client.getServerProxy().startServerAsync(params).get();
        } catch (InterruptedException | ExecutionException ex) {
            UIHelper.executeInUI(() -> apiError(ex, ERROR_STARTING_SERVER));
            return;
        }

        if( response == null || !response.getStatus().isOK()) {
            UIHelper.executeInUI(() -> statusError(response.getStatus(), ERROR_STARTING_SERVER));
        } else {
            connectDebugger(response, handle);
        }
    }
    private static void connectDebugger(StartServerResponse stat, ServerHandle handle) {
        String host = stat.getDetails().getProperties().get(DEBUG_DETAILS_HOST);
        String port = stat.getDetails().getProperties().get(DEBUG_DETAILS_PORT);
        String type = stat.getDetails().getProperties().get(DEBUG_DETAILS_TYPE);
        if( port == null || port.isEmpty() )
            return;

        PortFinder.waitForServer(host, Integer.parseInt(port), 5000);

        if(DEBUG_DETAILS_TYPE_JAVA.equals(type)) {
            String configurationName = handle.getId() + " Remote Debug";
            RunnerAndConfigurationSettings runSettings = getSettings(host, port, configurationName);

            // Connect java debugger
            ApplicationManager.getApplication().invokeLater(
                    () -> {
                        ExecutionEnvironment env = getEnvironment(runSettings);
                        if( env != null ) {
                            try {
                                Objects.requireNonNull(
                                        ProgramRunnerUtil.getRunner(
                                                DefaultDebugExecutor.getDebugExecutorInstance().getId(),
                                                runSettings)).execute(env);
                            } catch (com.intellij.execution.ExecutionException e) {
                            }
                        }
                    });
        }
    }

    private static ExecutionEnvironment getEnvironment(RunnerAndConfigurationSettings runSettings) {
        try {
            return ExecutionEnvironmentBuilder.create(
                    DefaultDebugExecutor.getDebugExecutorInstance(), runSettings).build();
        } catch (com.intellij.execution.ExecutionException e) {

        }
        return null;
    }
    public static final String DEBUG_DETAILS_HOST = "debug.details.host";
    public static final String DEBUG_DETAILS_PORT = "debug.details.port";
    public static final String DEBUG_DETAILS_TYPE = "debug.details.type";
    public static final String DEBUG_DETAILS_TYPE_JAVA = "java";


    private static RunnerAndConfigurationSettings getSettings(String host, String port, String configurationName) {
        ConfigurationType type = RemoteConfigurationType.getInstance();
        final Project project = ProjectManager.getInstance().getOpenProjects()[0];
        RunManager runManager = RunManager.getInstance(project);
        RunnerAndConfigurationSettings runSettings = runManager.findConfigurationByTypeAndName(
                type.getId(), configurationName);
        if( runSettings == null ) {
            runSettings = runManager.createConfiguration(
                    configurationName, type.getConfigurationFactories()[0]);
        } else {
            runManager.removeConfiguration(runSettings);
        }
        if (runSettings.getConfiguration() instanceof RemoteConfiguration) {
            RemoteConfiguration remoteConfiguration = (RemoteConfiguration) runSettings.getConfiguration();
            remoteConfiguration.HOST = (host == null ? "localhost" : host);
            remoteConfiguration.PORT = port;
        }
        runSettings.getConfiguration().setAllowRunningInParallel(true);
        runManager.addConfiguration(runSettings);
        return runSettings;
    }
}
