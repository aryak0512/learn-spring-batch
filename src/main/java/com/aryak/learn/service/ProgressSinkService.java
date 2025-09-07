package com.aryak.learn.service;

import com.aryak.learn.model.ProgressEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class ProgressSinkService {
    private final ConcurrentMap<String, Sinks.Many<ProgressEvent>> sinks = new ConcurrentHashMap<>();
    // keep last snapshot for quick replay if needed
    private final ConcurrentMap<String, ProgressEvent> lastSnapshot = new ConcurrentHashMap<>();

    public Sinks.Many<ProgressEvent> createSink(String jobExecutionId) {
        return sinks.computeIfAbsent(jobExecutionId, id -> {
            Sinks.Many<ProgressEvent> s = Sinks.many().replay().latest();
            // optionally push initial state
            return s;
        });
    }

    public void publish(String jobExecutionId, ProgressEvent ev) {
        lastSnapshot.put(jobExecutionId, ev);
        Sinks.Many<ProgressEvent> s = sinks.computeIfAbsent(jobExecutionId, id -> Sinks.many().replay().latest());
        s.tryEmitNext(ev);
    }

    public Flux<ProgressEvent> subscribe(String jobExecutionId) {
        Sinks.Many<ProgressEvent> sink = createSink(jobExecutionId);
        return sink.asFlux();
    }

    public Optional<ProgressEvent> last(String jobExecutionId) {
        return Optional.ofNullable(lastSnapshot.get(jobExecutionId));
    }

    public void complete(String jobExecutionId) {
        Sinks.Many<ProgressEvent> s = sinks.remove(jobExecutionId);
        if ( s != null ) s.tryEmitComplete();
        lastSnapshot.remove(jobExecutionId);
    }

    public void fail(String jobExecutionId, Throwable t) {
        Sinks.Many<ProgressEvent> s = sinks.remove(jobExecutionId);
        if ( s != null ) s.tryEmitError(t);
        lastSnapshot.remove(jobExecutionId);
    }
}