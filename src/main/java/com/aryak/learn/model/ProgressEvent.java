package com.aryak.learn.model;

import java.time.Duration;
import java.time.Instant;

public record ProgressEvent(
        String jobExecutionId,
        String fileName,
        long readCount,
        long writeCount,
        long skipCount,
        long filterCount,
        double percentComplete,      // 0..100 - if total known
        Duration elapsed,
        Duration estimatedRemaining,
        Instant timestamp
) {
}
