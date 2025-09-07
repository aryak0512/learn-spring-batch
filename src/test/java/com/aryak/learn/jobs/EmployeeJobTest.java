package com.aryak.learn.jobs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBatchTest
@SpringBootTest
class EmployeeJobTest {

    @Autowired
    Job employeeJob; // Job under test

    @Autowired
    JobLauncherTestUtils jobLauncher;

    @Autowired
    JobRepositoryTestUtils jobRepository;

    @BeforeEach
    public void cleanUp() {

        // Clean up old executions before running
        jobRepository.removeJobExecutions();

    }

    @Test
    @DisplayName(value = "Test if employee job gets triggered correctly")
    void testEmployeeJob() throws Exception {

        // given
        jobLauncher.setJob(employeeJob);

        // when
        JobExecution jobExecution = jobLauncher.launchJob();
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(jobExecution.getAllFailureExceptions().isEmpty());

        // then
        var stepCount = jobExecution.getStepExecutions().size();
        assertThat(stepCount).isEqualTo(1);
        StepExecution stepExecution = jobExecution.getStepExecutions().stream().toList().getLast();

        assertThat(stepExecution.getReadCount()).isEqualTo(5);
        assertThat(stepExecution.getWriteCount()).isEqualTo(2);
        assertThat(stepExecution.getFilterCount()).isEqualTo(3);
        assertThat(stepExecution.getSkipCount()).isEqualTo(0);
        assertThat(stepExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

    }

}
