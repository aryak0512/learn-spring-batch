package com.aryak.learn.config;


import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Endpoint(id = "batch")
public class BatchEndpoint {

    private final JobExplorer jobExplorer;

    public BatchEndpoint(JobExplorer jobExplorer) {
        this.jobExplorer = jobExplorer;
    }

    @ReadOperation
    public Map<String, Object> jobs() {
        List<Map<String, Object>> jobs = new ArrayList<>();

        for ( String jobName : jobExplorer.getJobNames() ) {
            Map<String, Object> jobMap = new HashMap<>();
            jobMap.put("jobName", jobName);

            List<Map<String, Object>> executionsList = new ArrayList<>();
            List<JobInstance> instances = jobExplorer.getJobInstances(jobName, 0, 5); // last 5 instances
            for ( JobInstance instance : instances ) {
                for ( JobExecution exec : jobExplorer.getJobExecutions(instance) ) {
                    Map<String, Object> execMap = new HashMap<>();
                    execMap.put("executionId", exec.getId());
                    execMap.put("status", exec.getStatus().toString());
                    execMap.put("startTime", exec.getStartTime());
                    execMap.put("endTime", exec.getEndTime());

                    List<Map<String, Object>> stepsList = new ArrayList<>();
                    for ( StepExecution step : exec.getStepExecutions() ) {
                        Map<String, Object> stepMap = new HashMap<>();
                        stepMap.put("stepName", step.getStepName());
                        stepMap.put("status", step.getStatus().toString());
                        stepMap.put("readCount", step.getReadCount());
                        stepMap.put("writeCount", step.getWriteCount());
                        stepMap.put("startTime", step.getStartTime());
                        stepMap.put("endTime", step.getEndTime());
                        stepsList.add(stepMap);
                    }

                    execMap.put("steps", stepsList);
                    executionsList.add(execMap);
                }
            }

            jobMap.put("executions", executionsList);
            jobs.add(jobMap);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("jobs", jobs);
        return response;
    }
}