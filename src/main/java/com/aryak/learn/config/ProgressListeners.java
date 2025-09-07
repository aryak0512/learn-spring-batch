package com.aryak.learn.config;

import com.aryak.learn.model.ProgressTracker;
import com.aryak.learn.service.ProgressSinkService;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.stereotype.Component;

@Component
@StepScope
public class ProgressListeners implements ItemReadListener<Object>, ItemWriteListener<Object>, SkipListener<Object, Object>, StepExecutionListener {

    private final ProgressTracker tracker;
    private final ProgressSinkService sinkService;

    public ProgressListeners(ProgressTracker tracker, ProgressSinkService sinkService) {
        this.tracker = tracker;
        this.sinkService = sinkService;
    }

    private void publish() {
        // build ProgressEvent from tracker and push to sinkService
    }

    private void publishIfNeeded() {
        // throttle logic or simply call publish()
        publish();
    }

    @Override
    public void afterRead(Object item) {
        tracker.incrementRead();
        publishIfNeeded();
    }

    @Override
    public void onReadError(Exception ex) {
        // handle read error
        publishIfNeeded();
    }

    @Override
    public void afterWrite(Chunk<? extends Object> items) {
        tracker.incrementWrite(items.size());
        publishIfNeeded();
    }

    @Override
    public void onWriteError(Exception exception, Chunk<? extends Object> items) {
        // handle write error (signature must be Exception, List<? extends T>)
        publishIfNeeded();
    }

    // ---- SkipListener ----
    // NOTE: parameter is Throwable (not Exception)
    @Override
    public void onSkipInRead(Throwable t) {
        tracker.incrementSkip();
        publishIfNeeded();
    }

    @Override
    public void onSkipInWrite(Object item, Throwable t) {
        tracker.incrementSkip();
        publishIfNeeded();
    }

    @Override
    public void onSkipInProcess(Object item, Throwable t) {
        tracker.incrementSkip();
        publishIfNeeded();
    }

    // ---- StepExecutionListener ----
    @Override
    public void beforeStep(StepExecution stepExecution) {
        tracker.markStart();
        // optional: set total items
        publish();
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        publish();
        sinkService.complete(String.valueOf(stepExecution.getJobExecutionId()));
        return stepExecution.getExitStatus();
    }
}