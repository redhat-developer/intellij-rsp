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
package org.jboss.tools.intellij.rsp.util;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.ServerProcessOutput;

import java.io.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Used to wrap output from a remote server process into something
 * usable by the terminal view.
 */
public class RemoteServerProcess extends Process {
    private boolean terminated = false;
    private OutputStream serverSysIn;
    private PipedOutputStream serverSysOutInternal;
    private PipedOutputStream serverSysErrInternal;
    private PipedInputStream serverSysOut;
    private PipedInputStream serverSysErr;
    private RSPThread writer;
    public RemoteServerProcess() {
        serverSysIn = new OutputStream() { @Override public void write(int b) { } };
        serverSysOut =new PipedInputStream();
        serverSysErr =new PipedInputStream();
        serverSysOutInternal = new PipedOutputStream();
        serverSysErrInternal = new PipedOutputStream();
        try {
            serverSysOut.connect(serverSysOutInternal);
            serverSysErr.connect(serverSysErrInternal);
        } catch(IOException ioe) {
            // TODO ?
        }
    }

    private class RSPThread extends Thread {
        private BlockingQueue<ServerProcessOutput> queue;
        public RSPThread() {
            super("Server Process Output Processor");
            queue = new LinkedBlockingQueue<ServerProcessOutput>();
        }
        public void run() {
            try {
                while (true) {
                    handleEventInternal(queue.take());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        public void addElement(ServerProcessOutput out) {
            if( out != null )
                queue.add(out);
        }
    }

    public void handleEvent(ServerProcessOutput output) {
        if( writer == null ) {
            writer = new RSPThread();
            writer.start();
        }
        writer.addElement(output);
    }

    private void handleEventInternal(ServerProcessOutput output) {
        try {
            if (output.getStreamType() == ServerManagementAPIConstants.STREAM_TYPE_SYSOUT) {
                serverSysOutInternal.write(output.getText().getBytes());
            }
            if (output.getStreamType() == ServerManagementAPIConstants.STREAM_TYPE_SYSERR) {
                serverSysErrInternal.write(output.getText().getBytes());
            }
        } catch(IOException ioe) {
            // TODO
        }
    }

    @Override
    public OutputStream getOutputStream() {
        return serverSysIn;
    }

    @Override
    public InputStream getInputStream() {
        return serverSysOut;
    }

    @Override
    public InputStream getErrorStream() {
        return serverSysErr;
    }

    @Override
    public int waitFor() throws InterruptedException {
        // TODO fix this
        while( !isTerminated()) {
            try {
                Thread.sleep(5000);
            } catch(InterruptedException ie) {
                throw ie;
            }
        }
        return 0;
    }

    @Override
    public int exitValue() {
        if( isTerminated() )
            return 0;
        throw new IllegalThreadStateException("Server not terminated yet");
    }

    @Override
    public void destroy() {

    }

    public synchronized void terminate() {
        terminated = true;
        try {
            serverSysIn.close();
        } catch(IOException ioe) {
        }
        try {
            serverSysOut.close();
        } catch(IOException ioe) {
        }
        try {
            serverSysErr.close();
        } catch(IOException ioe) {
        }
        try {
            serverSysOutInternal.close();
        } catch(IOException ioe) {
        }
        try {
            serverSysErrInternal.close();
        } catch(IOException ioe) {
        }
        writer.interrupt();
    }
    private synchronized boolean isTerminated() {
        return terminated;
    }
}