package com.aryak.learn;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class LearnSpringBatchApplication {

    private final Job job;
    private final JobLauncher jobLauncher;

    public LearnSpringBatchApplication(final Job job, final JobLauncher jobLauncher) {
        this.job = job;
        this.jobLauncher = jobLauncher;
    }

    public static void main(String[] args) {
        SpringApplication.run(LearnSpringBatchApplication.class, args);
    }


    // do something at startup
    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            jobLauncher.run(job, new JobParameters());

        };
    }

}
