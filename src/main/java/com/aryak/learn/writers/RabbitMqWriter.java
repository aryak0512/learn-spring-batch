package com.aryak.learn.writers;

import com.aryak.learn.model.EmployeeProcessed;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RabbitMqWriter implements ItemWriter<EmployeeProcessed> {

    private final RabbitTemplate rabbitTemplate;

    private final ObjectMapper objectMapper;

    public RabbitMqWriter(final RabbitTemplate rabbitTemplate, final ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void write(final Chunk<? extends EmployeeProcessed> chunk) throws Exception {

        try {

            for ( EmployeeProcessed e : chunk ) {
                String json = objectMapper.writeValueAsString(e);
                rabbitTemplate.convertAndSend("employee_exchange", "k1", json);
            }
        } catch (AmqpException e) {
            rethrow(e);
        }

    }

    private void rethrow(final AmqpException e) {
        log.error("Rabbit MQ exception occurred : ", e);
        throw new RuntimeException("Rabbit MQ exception");
    }
}
