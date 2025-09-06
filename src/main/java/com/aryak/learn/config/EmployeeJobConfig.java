package com.aryak.learn.config;

import com.aryak.learn.model.Employee;
import com.aryak.learn.model.EmployeeProcessed;
import com.aryak.learn.processors.EmployeeEmailProcessor;
import com.aryak.learn.processors.EmployeeLocationProcessor;
import com.aryak.learn.readers.EmployeeReader;
import com.aryak.learn.writers.EmployeeWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Configuration
public class EmployeeJobConfig {

    private final EmployeeReader employeeReader;
    private final EmployeeWriter employeeWriter;
    private final JobRepository jobRepository;
    private final EmployeeLocationProcessor employeeLocationProcessor;
    private final PlatformTransactionManager platformTransactionManager;

    public EmployeeJobConfig(final EmployeeReader employeeReader,
                             final EmployeeWriter employeeWriter,
                             final JobRepository jobRepository,
                             final EmployeeLocationProcessor employeeLocationProcessor,
                             final PlatformTransactionManager platformTransactionManager) {
        this.employeeReader = employeeReader;
        this.employeeWriter = employeeWriter;
        this.jobRepository = jobRepository;
        this.employeeLocationProcessor = employeeLocationProcessor;
        this.platformTransactionManager = platformTransactionManager;
    }

    @Bean
    public Job employeeJob(Step employeeStep) {
        return new JobBuilder("employeeJob", jobRepository)
                .start(employeeStep)
                .build();
    }

    @Bean
    public Step employeeStep(CompositeItemProcessor<Employee, EmployeeProcessed> compositeItemProcessor) {
        return new StepBuilder("employeeStep", jobRepository)
                .<Employee, EmployeeProcessed>chunk(10, platformTransactionManager)
                .reader(employeeReader)
                .processor(compositeItemProcessor)
                .writer(employeeWriter)
                .build();
    }

    @Bean
    public CompositeItemProcessor<Employee, EmployeeProcessed> compositeItemProcessor() {
        CompositeItemProcessor<Employee, EmployeeProcessed> compositeItemProcessor = new CompositeItemProcessor<>();
        var processors = List.of(new EmployeeLocationProcessor(), new EmployeeEmailProcessor());
        compositeItemProcessor.setDelegates(processors);
        return compositeItemProcessor;
    }
}
