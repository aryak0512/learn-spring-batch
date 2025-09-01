package com.aryak.learn.writers;

import com.aryak.learn.model.Employee;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
public class EmployeeWriter implements ItemWriter<Employee> {

    private final ObjectMapper objectMapper;

    public EmployeeWriter(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void write(final Chunk<? extends Employee> chunk) throws Exception {

        // convert to JSON
        var s = objectMapper.writeValueAsString(chunk);
        System.out.println(s);
    }
}
