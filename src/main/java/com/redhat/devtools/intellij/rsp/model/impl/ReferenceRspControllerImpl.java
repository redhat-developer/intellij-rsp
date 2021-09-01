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
package com.redhat.devtools.intellij.rsp.model.impl;

import com.intellij.execution.process.BaseProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.rsp.model.*;
import com.redhat.devtools.intellij.rsp.util.ExecUtilClone;
import com.redhat.devtools.intellij.rsp.util.JavaUtils;
import com.redhat.devtools.intellij.rsp.util.PortFinder;
import com.redhat.devtools.intellij.rsp.util.ProcessMonitorThread;
import com.redhat.devtools.intellij.rsp.util.RspProcessHandler;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Provides the logic to start and stop a reference-implementation-based RSP
 * including launching the felix command with flags for logging, port,
 * data directory, and data locking.
 */
public class ReferenceRspControllerImpl implements IRspStateController {
    private IRspType serverType;
    private int portMin;
    private int portMax;

    private Process runningProcess;

    public ReferenceRspControllerImpl(IRspType rspServerType, int portMin, int portMax) {
        this.serverType = rspServerType;
        this.portMin = portMin;
        this.portMax = portMax;
    }

    @Override
    public ServerConnectionInfo start(IRspStartCallback callback) throws StartupFailedException {
        String rspHome = serverType.getServerHome();
        File rspHomeFile = new File(rspHome);
        if( !rspHomeFile.exists() || !rspHomeFile.isDirectory())
            throw new StartupFailedException("RSP does not appear to be installed.");

        File felixFile = new File(new File(rspHomeFile, "bin"), "felix.jar");
        if( !felixFile.exists() || !felixFile.isFile())
            throw new StartupFailedException("RSP does not appear to be installed or is broken. Please use the Download / Update RSP action.");

        int port = new PortFinder().nextFreePort(portMin, portMax);
        if( port == -1 )
            throw new StartupFailedException("No free port within the defined range found.");

        File java = JavaUtils.findJavaExecutable();
        if( java == null || !java.exists())
            throw new StartupFailedException("A java executable could not be located on this system.");

        String portInUse = getLockedWorkspacePort();
        if( portInUse != null) {
            callback.updateRspState(IRspCore.IJServerState.STARTED);
            return new ServerConnectionInfo("localhost", Integer.parseInt(portInUse));
        }
        BaseProcessHandler handler = startRSP(rspHome, port, java, callback);
        if( handler != null ) {
            setRunningProcess(handler.getProcess());
            try {
                Project project = ProjectManager.getInstance().getOpenProjects()[0];
                try {
                    String name = serverType.getId();
                    ExecHelper.linkProcessToTerminal(handler, project, name, false);
                } catch(Throwable t) {
                    t.printStackTrace();
                }
            } catch (Throwable ioe) {
                // TODO
            }

            boolean started = waitForPortInUse(port);
            if (started) {
                callback.updateRspState(IRspCore.IJServerState.STARTED);
                return new ServerConnectionInfo("localhost", port);
            } else {
                terminate(callback);
                throw new StartupFailedException("Unable to connect to RSP after startup.");
            }
        }
        return null;
    }

    private synchronized void setRunningProcess(Process p) {
        this.runningProcess = p;
    }

    private synchronized Process getRunningProcess() {
        return this.runningProcess;
    }

    private boolean waitForPortInUse(int port) {
        long time = System.currentTimeMillis();
        while(System.currentTimeMillis() < (time + 60000)) {
            if( !PortFinder.isLocalPortFree(port))
                return true;
            try {
                Thread.sleep(1000);
            } catch(InterruptedException ie) {
                // Ignore
            }
        }
        return false;
    }

    private BaseProcessHandler startRSP(String rspHome, int port, File java, IRspStartCallback callback) {
        callback.updateRspState(IRspCore.IJServerState.STARTING);
        File workingDir = new File(rspHome);
        File felix = new File( new File(workingDir, "bin"), "felix.jar");

        String cmd = java.getAbsolutePath();
        String portFlag = "-Drsp.server.port=" + port;
        String id = "-Dorg.jboss.tools.rsp.id=" + serverType.getId();
        String logbackFlag =  "-Dlogback.configurationFile=./conf/logback.xml";
        String jar = "-jar";

        String[] cmdArr = new String[] {cmd, portFlag, id, logbackFlag, jar, felix.getAbsolutePath()};
        String cmdLine = String.join(" ", cmdArr);
        try {
            Process p = Runtime.getRuntime().exec(cmdArr, null, workingDir);
            ProcessMonitorThread pmt = new ProcessMonitorThread(p, (Process proc9) -> {
                callback.updateRspState(IRspCore.IJServerState.STOPPED);
                setRunningProcess(null);
            });
            pmt.start();
            return new RspProcessHandler(p, "Run Felix", cmdLine);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void terminate(IRspStartCallback callback) {
        Process p = getRunningProcess();
        if( p != null )
            p.destroy();
        setRunningProcess(null);
        callback.updateRspState(IRspCore.IJServerState.STOPPED);
    }

    private String getLockedWorkspacePort() {
        File lockFile = getLockFile();
        if( !lockFile.exists())
            return null;
        Path p = lockFile.toPath();
        String portInUse = null;
        try {
            portInUse = new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
        } catch (IOException e) {
        }
        if( portInUse == null || portInUse.isEmpty() )
            return null;
        if( PortFinder.isLocalPortFree(Integer.parseInt(portInUse))) {
            lockFile.delete();
            return null;
        }
        return portInUse;
    }
    private boolean isWorkspaceLocked() {
        return getLockedWorkspacePort() == null ? false : true;
    }
    private File getLockFile() {
        String userHome = JavaUtils.getUserHome();
        return new File(userHome).toPath().resolve(".rsp").resolve(serverType.getId()).resolve(".lock").toFile();
    }

}
