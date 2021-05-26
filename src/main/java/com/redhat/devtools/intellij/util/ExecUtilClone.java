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
package com.redhat.devtools.intellij.util;

import com.intellij.execution.TaskExecutor;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessWaitFor;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;

import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.jediterm.terminal.ProcessTtyConnector;
import com.jediterm.terminal.TtyConnector;
import com.pty4j.PtyProcess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.terminal.AbstractTerminalRunner;
import org.jetbrains.plugins.terminal.LocalTerminalDirectRunner;
import org.jetbrains.plugins.terminal.TerminalOptionsProvider;
import org.jetbrains.plugins.terminal.TerminalView;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * To be deleted once my link-to-terminal is properly in commons
 */
public class ExecUtilClone {
    private static class RedirectedStream extends FilterInputStream {
        private boolean emitLF = false;
        private final boolean redirect;
        private final boolean delay;

        private RedirectedStream(InputStream delegate, boolean redirect, boolean delay) {
            super(delegate);
            this.redirect = redirect;
            this.delay = delay;
        }

        @Override
        public synchronized int read() throws IOException {
            if (emitLF) {
                emitLF = false;
                return '\n';
            } else {
                int c = super.read();
                if (redirect && c == '\n') {
                    emitLF = true;
                    c = '\r';
                }
                return c;
            }
        }

        @Override
        public synchronized int read(@NotNull byte[] b) throws IOException {
            return read(b, 0, b.length);
        }

        @Override
        public synchronized int read(@NotNull byte[] b, int off, int len) throws IOException {
            if (b == null) {
                throw new NullPointerException();
            } else if (off < 0 || len < 0 || len > b.length - off) {
                throw new IndexOutOfBoundsException();
            } else if (len == 0) {
                return 0;
            }

            int c = read();
            if (c == -1) {
                if (delay) {
                    try {
                        Thread.sleep(60000L);
                    } catch (InterruptedException e) {}
                }
                return -1;
            }
            b[off] = (byte)c;

            int i = 1;
            try {
                for (; i < len  && available() > 0; i++) {
                    c = read();
                    if (c == -1) {
                        break;
                    }
                    b[off + i] = (byte)c;
                }
            } catch (IOException ee) {}
            return i;
        }
    }
    private static class RedirectedProcess extends Process {
        private final Process delegate;
        private final InputStream inputStream;

        private RedirectedProcess(Process delegate, boolean redirect, boolean delay) {
            this.delegate = delegate;
            inputStream = new RedirectedStream(delegate.getInputStream(), redirect, delay) {};
        }

        @Override
        public OutputStream getOutputStream() {
            return delegate.getOutputStream();
        }

        @Override
        public InputStream getInputStream() {
            return inputStream;
        }

        @Override
        public InputStream getErrorStream() {
            return delegate.getErrorStream();
        }

        @Override
        public int waitFor() throws InterruptedException {
            return delegate.waitFor();
        }

        @Override
        public boolean waitFor(long timeout, TimeUnit unit) throws InterruptedException {
            return delegate.waitFor(timeout, unit);
        }

        @Override
        public int exitValue() {
            return delegate.exitValue();
        }

        @Override
        public void destroy() {
            delegate.destroy();
        }

        @Override
        public Process destroyForcibly() {
            return delegate.destroyForcibly();
        }

        @Override
        public boolean isAlive() {
            return delegate.isAlive();
        }
    }

    private static class RspProcessHandler extends ProcessHandler implements TaskExecutor {

        private final Process myProcess;
        private final ProcessWaitFor myWaitFor;

        RspProcessHandler(Process process, @NotNull String presentableName) {
            myProcess = process;
            myWaitFor = new ProcessWaitFor(process, this, presentableName);
        }

        @Override
        public void startNotify() {
            addProcessListener(new ProcessAdapter() {
                @Override
                public void startNotified(@NotNull ProcessEvent event) {
                    try {
                        myWaitFor.setTerminationCallback(integer -> notifyProcessTerminated(integer));
                    }
                    finally {
                        removeProcessListener(this);
                    }
                }
            });

            super.startNotify();
        }

        @Override
        protected void destroyProcessImpl() {
            myProcess.destroy();
        }

        @Override
        protected void detachProcessImpl() {
            destroyProcessImpl();
        }

        @Override
        public boolean detachIsDefault() {
            return false;
        }

        @Override
        public boolean isSilentlyDestroyOnClose() {
            return true;
        }

        @Nullable
        @Override
        public OutputStream getProcessInput() {
            return myProcess.getOutputStream();
        }

        @NotNull
        @Override
        public Future<?> executeTask(@NotNull Runnable task) {
            return AppExecutorUtil.getAppExecutorService().submit(task);
        }
    }

    private static class RspTerminalRunner extends AbstractTerminalRunner {
        private final String title;
        private final Process process;

        public RspTerminalRunner(Project project, String title, Process process) {
            super(project);
            this.process = process;
            this.title = title;
        }
        @Override
        protected Process createProcess(@Nullable String s) {
            return process;
        }

        @Override
        public ProcessHandler createProcessHandler(Process process) {
            return new RspProcessHandler(process, title);
        }

        @Override
        protected String getTerminalConnectionName(Process process) {
            return null;
        }

        @Override
        protected TtyConnector createTtyConnector(Process process) {
            return new ProcessTtyConnector(process, StandardCharsets.UTF_8) {
                @Override
                protected void resizeImmediately() {
                }

                @Override
                public String getName() {
                    return title;
                }

                @Override
                public boolean isConnected() {
                    return process.isAlive();
                }
            };
        }

        @Override
        public String runningTargetName() {
            return null;
        }
    }
    private static RspTerminalRunner createTerminalRunner(Project project, Process process, String title) {
        return new RspTerminalRunner(project, title, process);
    }

    /**
     * Ensure the terminal window tab is created. This is required because some IJ editions (2018.3) do not
     * initialize this window when you create a TerminalView through {@link #linkProcessToTerminal(Process, Project, String, boolean)}
     *
     * @param project the IJ project
     */
    public static void ensureTerminalWindowsIsOpened(Project project) {
        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Terminal");
        if (toolWindow != null) {
            ApplicationManager.getApplication().invokeAndWait(() -> toolWindow.show(null));
        }
    }


    public static void linkProcessToTerminal(Process p, Project project, String title, boolean waitForProcessExit) throws IOException {
        try {
            ensureTerminalWindowsIsOpened(project);
            boolean isPost2018_3 = ApplicationInfo.getInstance().getBuild().getBaselineVersion() >= 183;
            final RedirectedProcess process = new RedirectedProcess(p, true, isPost2018_3);
            RspTerminalRunner runner = createTerminalRunner(project, process, title);

            TerminalOptionsProvider terminalOptions = ServiceManager.getService(TerminalOptionsProvider.class);
            terminalOptions.setCloseSessionOnLogout(false);
            final TerminalView view = TerminalView.getInstance(project);
            final Method[] method = new Method[1];
            final Object[][] parameters = new Object[1][];
            try {
                method[0] = TerminalView.class.getMethod("createNewSession", new Class[] {Project.class, AbstractTerminalRunner.class});
                parameters[0] = new Object[] {project, runner};
            } catch (NoSuchMethodException e) {
                try {
                    method[0] = TerminalView.class.getMethod("createNewSession", new Class[] {AbstractTerminalRunner.class});
                    parameters[0] = new Object[] { runner};
                } catch (NoSuchMethodException e1) {
                    throw new IOException(e1);
                }
            }
            runner.createProcessHandler(p).startNotify();
            ApplicationManager.getApplication().invokeLater(() -> {
                try {
                    method[0].invoke(view, parameters[0]);
                } catch (IllegalAccessException| InvocationTargetException e) {}
            });
            if (waitForProcessExit && p.waitFor() != 0) {
                throw new IOException("Process returned exit code: " + p.exitValue(), null);
            }
        } catch (IOException e) {
            throw e;
        }
        catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    public static void executeWithTerminal(Project project, String title, File workingDirectory, boolean waitForProcessToExit, String... command) throws IOException {
        try {
            ProcessBuilder builder = new ProcessBuilder(command).directory(workingDirectory).redirectErrorStream(true);
            Process p = builder.start();
            linkProcessToTerminal(p, project, title, waitForProcessToExit);
        } catch (IOException e) {
            throw e;
        }
    }
}
