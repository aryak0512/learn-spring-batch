package com.aryak.learn.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDateTime;

@Slf4j
@Component
public class FtpJobLauncher {

    private final JobLauncher jobLauncher;

    private final Job employeeJob;

    public FtpJobLauncher(final JobLauncher jobLauncher, final Job employeeJob) {
        this.jobLauncher = jobLauncher;
        this.employeeJob = employeeJob;
    }

    @ServiceActivator(inputChannel = "ftpChannel")
    public void triggerBatchJob(File file) throws Exception {
        log.info("=== FTP File Downloaded ===");
        log.info("File name: '{}', path: '{}', size: {} bytes", file.getName(), file.getAbsolutePath(), file.length());
        JobParametersBuilder jobParamsBuilder = new JobParametersBuilder();
        jobParamsBuilder.addJobParameter("filePath", file.getAbsolutePath(), String.class);
        jobParamsBuilder.addJobParameter("jobRun", LocalDateTime.now(), LocalDateTime.class);
        jobLauncher.run(employeeJob, jobParamsBuilder.toJobParameters());
    }

}
