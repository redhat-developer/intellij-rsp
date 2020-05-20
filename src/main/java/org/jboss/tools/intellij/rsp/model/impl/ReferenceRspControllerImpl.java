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
package org.jboss.tools.intellij.rsp.model.impl;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.jboss.tools.intellij.rsp.model.*;
import org.jboss.tools.intellij.rsp.util.ExecUtilClone;
import org.jboss.tools.intellij.rsp.util.JavaUtils;
import org.jboss.tools.intellij.rsp.util.PortFinder;
import org.jboss.tools.intellij.rsp.util.ProcessMonitorThread;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ReferenceRspControllerImpl implements IRspStateController {
    private IRspType serverType;
    private String version;
    private int portMin;
    private int portMax;

    private Process runningProcess;

    public ReferenceRspControllerImpl(IRspType rspServerType, String version, int portMin, int portMax) {
        this.serverType = rspServerType;
        this.version = version;
        this.portMin = portMin;
        this.portMax = portMax;
    }

    @Override
    public ServerConnectionInfo start(IRspStartCallback callback) {
        String rspHome = serverType.getServerHome();
        File rspHomeFile = new File(rspHome);
        if( !rspHomeFile.exists() || !rspHomeFile.isDirectory())
            return null;

        int port = new PortFinder().nextFreePort(portMin, portMax);
        if( port == -1 )
            return null;

        File java = JavaUtils.findJavaExecutable();
        if( java == null || !java.exists())
            return null;

        String portInUse = getLockedWorkspacePort();
        if( portInUse != null) {
            return new ServerConnectionInfo("localhost", Integer.parseInt(portInUse));
        }
        Process p = startRSP(rspHome, port, java, callback);
        if( p != null ) {
            setRunningProcess(p);
            try {
                Project project = ProjectManager.getInstance().getOpenProjects()[0];
                try {
                    ExecUtilClone.linkProcessToTerminal(p, project, serverType.getId(), false);
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

    private Process startRSP(String rspHome, int port, File java, IRspStartCallback callback) {
        callback.updateRspState(IRspCore.IJServerState.STARTING);
        File workingDir = new File(rspHome);
        File felix = new File( new File(workingDir, "bin"), "felix.jar");

        String cmd = java.getAbsolutePath();
        String portFlag = "-Drsp.server.port=" + port;
        String id = "-Dorg.jboss.tools.rsp.id=" + serverType.getId();
        String logbackFlag =  "-Dlogback.configurationFile=./conf/logback.xml";
        String jar = "-jar";

        String[] cmdArr = new String[] {cmd, portFlag, id, logbackFlag, jar, felix.getAbsolutePath()};
        try {
            Process p = Runtime.getRuntime().exec(cmdArr, null, workingDir);
            ProcessMonitorThread pmt = new ProcessMonitorThread(p, (Process proc9) -> {
                callback.updateRspState(IRspCore.IJServerState.STOPPED);
                setRunningProcess(null);
            });
            pmt.start();
            return p;
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
