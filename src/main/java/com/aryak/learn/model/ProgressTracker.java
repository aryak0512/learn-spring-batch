package com.aryak.learn.model;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

@Component
@StepScope
public class ProgressTracker {

    private final AtomicLong read = new AtomicLong();
    private final AtomicLong write = new AtomicLong();
    private final AtomicLong skip = new AtomicLong();
    private final AtomicLong filter = new AtomicLong();
    private volatile Instant start = Instant.now();
    private volatile Long totalItems = null; // could be set if known

    public void setTotalItems(Long total) {
        this.totalItems = total;
    }

    public void markStart() {
        this.start = Instant.now();
    }

    public void incrementRead() {
        read.incrementAndGet();
    }

    public void incrementWrite(long n) {
        write.addAndGet(n);
    }

    public void incrementSkip() {
        skip.incrementAndGet();
    }

    public void incrementFilter() {
        filter.incrementAndGet();
    }

    public ProgressSnapshot snapshot(String jobExecutionId, String fileName) {
        long r = read.get();
        long w = write.get();
        long s = skip.get();
        long f = filter.get();
        Duration elapsed = Duration.between(start, Instant.now());
        double pct = totalItems == null ? -1.0 : ((double) r / totalItems) * 100.0;
        Duration eta = null;
        if ( totalItems != null && r > 0 ) {
            double rate = (double) r / Math.max(1, elapsed.toMillis()); // items / ms
            long remaining = totalItems - r;
            long millisRemaining = (long) (remaining / rate);
            eta = Duration.ofMillis(millisRemaining);
        }
        return new ProgressSnapshot(jobExecutionId, fileName, r, w, s, f, pct, elapsed, eta, Instant.now());
    }

    // simple DTO for internal use
    public record ProgressSnapshot(String jobExecutionId, String fileName, long read, long write,
                                   long skip, long filter, double pct, Duration elapsed, Duration eta, Instant ts) {
    }
}
