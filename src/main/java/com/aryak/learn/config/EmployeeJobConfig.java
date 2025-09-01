package com.aryak.learn.config;

import com.aryak.learn.model.Employee;
import com.aryak.learn.readers.EmployeeReader;
import com.aryak.learn.writers.EmployeeWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class EmployeeJobConfig {

    private final EmployeeReader employeeReader;
    private final EmployeeWriter employeeWriter;
    private final JobRepository jobRepository;

    private final PlatformTransactionManager platformTransactionManager;

    public EmployeeJobConfig(final EmployeeReader employeeReader,
                             final EmployeeWriter employeeWriter,
                             final JobRepository jobRepository,
                             final PlatformTransactionManager platformTransactionManager) {
        this.employeeReader = employeeReader;
        this.employeeWriter = employeeWriter;
        this.jobRepository = jobRepository;
        this.platformTransactionManager = platformTransactionManager;
    }

    @Bean
    public Job employeeJob(Step employeeStep) {
        return new JobBuilder("employeeJob", jobRepository)
                .start(employeeStep)
                .build();
    }

    @Bean
    public Step employeeStep() {
        return new StepBuilder("employeeStep", jobRepository)
                .<Employee, Employee>chunk(2, platformTransactionManager)
                .reader(employeeReader)
                // plug in the processor later
                .writer(employeeWriter)
                .build();
    }
}
