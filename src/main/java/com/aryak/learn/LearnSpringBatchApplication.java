package com.aryak.learn;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;

@SpringBootApplication
public class LearnSpringBatchApplication {

    private final Job employeeJob;
    private final JobLauncher jobLauncher;

    public LearnSpringBatchApplication(final Job employeeJob, final JobLauncher jobLauncher) {
        this.employeeJob = employeeJob;
        this.jobLauncher = jobLauncher;
    }

    public static void main(String[] args) {
        SpringApplication.run(LearnSpringBatchApplication.class, args);
    }


    // do something at startup
    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {

            JobParametersBuilder jobParamsBuilder = new JobParametersBuilder();
            // attach the key value pairs
            jobParamsBuilder.addJobParameter("tag", "xyz_dept", String.class);
            jobParamsBuilder.addJobParameter("dept_id", 122, Integer.class);
            jobParamsBuilder.addJobParameter("jobRun", LocalDateTime.now(), LocalDateTime.class);

            jobLauncher.run(employeeJob, jobParamsBuilder.toJobParameters());

        };
    }

}
