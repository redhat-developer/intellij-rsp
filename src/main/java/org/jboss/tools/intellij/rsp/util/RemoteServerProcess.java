package org.jboss.tools.intellij.rsp.util;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.ServerProcessOutput;

import java.io.*;

public class RemoteServerProcess extends Process {
    private boolean terminated = false;
    private OutputStream serverSysIn;
    private PipedOutputStream serverSysOutInternal;
    private PipedOutputStream serverSysErrInternal;
    private PipedInputStream serverSysOut;
    private PipedInputStream serverSysErr;

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

    public void handleEvent(ServerProcessOutput output) {
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
    }
    private synchronized boolean isTerminated() {
        return terminated;
    }
}