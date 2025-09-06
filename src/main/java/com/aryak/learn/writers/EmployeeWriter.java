package com.aryak.learn.writers;

import com.aryak.learn.model.EmployeeProcessed;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmployeeWriter implements ItemWriter<EmployeeProcessed> {

    private final ObjectMapper objectMapper;

    public EmployeeWriter(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void write(final Chunk<? extends EmployeeProcessed> chunk) throws Exception {

        var size = chunk.size();
        log.info("Size is : {}", size);
        // convert to JSON
        var s = objectMapper.writerWithDefaultPrettyPrinter().
                writeValueAsString(chunk);
        log.info("JSON : {}", s);
    }
}
