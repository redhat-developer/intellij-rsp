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

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import org.jboss.tools.intellij.rsp.model.IRsp;
import org.jboss.tools.rsp.api.dao.JobHandle;
import org.jboss.tools.rsp.api.dao.JobProgress;
import org.jboss.tools.rsp.api.dao.JobRemoved;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * A wrapper representing a job executing on an RSP that this extension
 * receives status updates for.
 */
public class RspProgressJob extends Task.Backgroundable {
    private IRsp rsp;
    private JobHandle jobHandle;

    private JobProgress progress;
    private JobRemoved jobRemoved;

    private CountDownLatch latch;
    public RspProgressJob(IRsp rsp, JobHandle jobHandle) {
        super(null, jobHandle.getName(), true);
        this.rsp = rsp;
        this.jobHandle = jobHandle;
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        latch = newLatch();
        while( !isDone() && !indicator.isCanceled()) {
            try {
                latch.await();
            } catch(InterruptedException ie) {
            }
            updateIndicator(indicator);
        }
    }
    @Override
    public void onCancel() {
        rsp.getModel().getClient(rsp).getServerProxy().cancelJob(jobHandle);
    }
    private synchronized void updateIndicator(ProgressIndicator indicator) {
        if( this.progress != null ) {
            double d = this.progress.getPercent();
            indicator.setFraction(d / 100);
            newLatch();
        }
    }

    private synchronized CountDownLatch getLatch() {
        return latch;
    }
    private synchronized CountDownLatch newLatch() {
        latch = new CountDownLatch(1);
        return latch;
    }
    private synchronized void countdown() {
        if( latch != null )
            latch.countDown();
    }

    private synchronized boolean isDone() {
        return jobRemoved != null;
    }
    public synchronized void setJobProgress(JobProgress jp) {
        this.progress = jp;
        countdown();
    }
    public synchronized void setJobRemoved(JobRemoved jr) {
        this.jobRemoved = jr;
        countdown();
    }
}
