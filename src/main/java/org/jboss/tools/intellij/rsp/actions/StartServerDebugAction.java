package org.jboss.tools.intellij.rsp.actions;

import com.intellij.execution.ProgramRunnerUtil;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.remote.RemoteConfiguration;
import com.intellij.execution.remote.RemoteConfigurationType;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ExecutionEnvironmentBuilder;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.jboss.tools.intellij.rsp.client.IntelliJRspClientLauncher;
import org.jboss.tools.intellij.rsp.model.IRsp;
import org.jboss.tools.intellij.rsp.model.impl.RspCore;
import org.jboss.tools.intellij.rsp.ui.tree.RspTreeModel;
import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.LaunchParameters;
import org.jboss.tools.rsp.api.dao.ServerAttributes;
import org.jboss.tools.rsp.api.dao.StartServerResponse;
import org.jboss.tools.rsp.api.dao.Status;

import javax.swing.tree.TreePath;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class StartServerDebugAction extends AbstractTreeAction {
    @Override
    protected boolean isEnabled(Object o) {
        if( o instanceof RspTreeModel.ServerStateWrapper) {
            int state = ((RspTreeModel.ServerStateWrapper)o).getServerState().getState();
            return state == ServerManagementAPIConstants.STATE_STOPPED || state == ServerManagementAPIConstants.STATE_UNKNOWN;
        }
        return false;
    }


    @Override
    protected void actionPerformed(AnActionEvent e, TreePath treePath, Object selected) {

        if (selected instanceof RspTreeModel.ServerStateWrapper) {
            final RspTreeModel.ServerStateWrapper sel = (RspTreeModel.ServerStateWrapper) selected;
            final Project project = ProjectManager.getInstance().getOpenProjects()[0];
            final IntelliJRspClientLauncher client = RspCore.getDefault().getClient(sel.getRsp());
            new Thread("Debugging server: " + sel.getServerState().getServer().getId()) {
                public void run() {
                    actionPerformedInternal(sel, project, client);
                }
            }.start();
        }
    }
    protected void actionPerformedInternal(RspTreeModel.ServerStateWrapper sel, Project project, IntelliJRspClientLauncher client) {
        String mode = "debug";
        ServerAttributes sa = new ServerAttributes(sel.getServerState().getServer().getType().getId(),
                sel.getServerState().getServer().getId(), new HashMap<String,Object>());
        LaunchParameters params = new LaunchParameters(sa, mode);

        try {
            StartServerResponse stat = client.getServerProxy().startServerAsync(params).get();
            if( !stat.getStatus().isOK()) {
                showError(stat.getStatus());
            } else {
                connectDebugger(stat);
            }
        } catch (InterruptedException ex) {
            showError(ex);
        } catch (ExecutionException ex) {
            showError(ex);
        }
    }
    private void connectDebugger(StartServerResponse stat) {
        String host = stat.getDetails().getProperties().get(DEBUG_DETAILS_HOST);
        String port = stat.getDetails().getProperties().get(DEBUG_DETAILS_PORT);
        String type = stat.getDetails().getProperties().get(DEBUG_DETAILS_TYPE);
        if( port == null || port.isEmpty() )
            return;

        if(DEBUG_DETAILS_TYPE_JAVA.equals(type)) {
            String configurationName = stat.getDetails().getCmdLine() + " Remote Debug";
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

    private ExecutionEnvironment getEnvironment(RunnerAndConfigurationSettings runSettings) {
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


    private RunnerAndConfigurationSettings getSettings(String host, String port, String configurationName) {
        ConfigurationType type = RemoteConfigurationType.getInstance();
        final Project project = ProjectManager.getInstance().getOpenProjects()[0];
        RunManager runManager = RunManager.getInstance(project);
        RunnerAndConfigurationSettings runSettings = runManager.findConfigurationByTypeAndName(
                type.getId(), configurationName);
        if( runSettings == null ) {
            runSettings = runManager.createConfiguration(
                    configurationName, type.getConfigurationFactories()[0]);
            if (runSettings.getConfiguration() instanceof RemoteConfiguration) {
                RemoteConfiguration remoteConfiguration = (RemoteConfiguration) runSettings.getConfiguration();
                remoteConfiguration.HOST = (host == null ? "localhost" : host);
                remoteConfiguration.PORT = port;
            }
            runSettings.getConfiguration().setAllowRunningInParallel(true);
            runManager.addConfiguration(runSettings);
        }
        return runSettings;
    }

    protected int getPortFromConfiguration(RunConfiguration configuration) {
        if (configuration instanceof RemoteConfiguration) {
            return Integer.parseInt(((RemoteConfiguration) configuration).PORT);
        }
        return -1;
    }

    private void showError(Status stat) {
        // TODO
    }
    private void showError(Exception stat) {
        // TODO
    }

}
