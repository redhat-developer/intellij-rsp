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

    private synchronized void updateIndicator(ProgressIndicator indicator) {
        double d = this.progress.getPercent();
        System.out.println("progress: " + d);
        indicator.setFraction(d/100);
        newLatch();
    }

    private synchronized CountDownLatch getLatch() {
        return latch;
    }
    private synchronized CountDownLatch newLatch() {
        latch = new CountDownLatch(1);
        return latch;
    }
    private synchronized void countdown() {
        latch.countDown();
    }

    private synchronized boolean isDone() {
        return jobRemoved != null; // TODO or is canceled
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
