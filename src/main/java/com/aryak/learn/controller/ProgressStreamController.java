package com.aryak.learn.controller;

import com.aryak.learn.model.ProgressEvent;
import com.aryak.learn.service.ProgressSinkService;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Optional;

@RestController
@RequestMapping("/stream")
public class ProgressStreamController {

    private final ProgressSinkService sinkService;

    public ProgressStreamController(ProgressSinkService sinkService) {
        this.sinkService = sinkService;
    }

    @GetMapping(value = "/{jobExecutionId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<ProgressEvent>> stream(@PathVariable String jobExecutionId) {
        Flux<ProgressEvent> flux = sinkService.subscribe(jobExecutionId)
                .doOnCancel(() -> { /* optionally record that UI unsubscribed */ });

        // Also emit last snapshot immediately (if present) using concat
        Optional<ProgressEvent> last = sinkService.last(jobExecutionId);
        Flux<ProgressEvent> initial = last.map(Flux::just).orElse(Flux.empty());

        return Flux.concat(initial, flux)
                .map(ev -> ServerSentEvent.<ProgressEvent>builder().id(ev.jobExecutionId()).data(ev).build());
    }
}
