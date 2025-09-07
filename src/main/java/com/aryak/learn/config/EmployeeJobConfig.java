package com.aryak.learn.config;

import com.aryak.learn.model.Employee;
import com.aryak.learn.model.EmployeeProcessed;
import com.aryak.learn.processors.EmployeeEmailProcessor;
import com.aryak.learn.processors.EmployeeLocationProcessor;
import com.aryak.learn.readers.EmployeeReader;
import com.aryak.learn.rowmappers.EmployeeRowMapper;
import com.aryak.learn.writers.EmployeeWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.extensions.excel.RowMapper;
import org.springframework.batch.extensions.excel.mapping.BeanWrapperRowMapper;
import org.springframework.batch.extensions.excel.poi.PoiItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
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
    public Step employeeStep(CompositeItemProcessor<Employee, EmployeeProcessed> compositeItemProcessor,
                             FlatFileItemReader<Employee> employeeFlatFileItemReader,
                             PoiItemReader<Employee> excelReader,
                             JsonItemReader<Employee> jsonItemReader) {
        return new StepBuilder("employeeStep", jobRepository)
                .<Employee, EmployeeProcessed>chunk(10, platformTransactionManager)
                .reader(jsonItemReader)
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

    @Bean
    public FlatFileItemReader<Employee> employeeFlatFileItemReader() {

        FlatFileItemReader<Employee> reader = new FlatFileItemReader<>();
        reader.setResource(new FileSystemResource("/Users/aryak/Downloads/learn-spring-batch/src/main/resources/employees.csv"));
        reader.setLinesToSkip(0);

        // specify delimiter and fields
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(",");
        tokenizer.setNames("id", "name", "location", "email");

        var lineMapper = getLineMapper(tokenizer);
        reader.setLineMapper(lineMapper);
        return reader;
    }

    private DefaultLineMapper<Employee> getLineMapper(DelimitedLineTokenizer tokenizer) {
        DefaultLineMapper<Employee> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(tokenizer);

        BeanWrapperFieldSetMapper<Employee> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Employee.class);

        lineMapper.setFieldSetMapper(fieldSetMapper);
        return lineMapper;
    }

    @Bean
    public PoiItemReader<Employee> excelReader() {
        PoiItemReader<Employee> employeePoiItemReader = new PoiItemReader<>();
        employeePoiItemReader.setLinesToSkip(1);
        employeePoiItemReader.setResource(new FileSystemResource("/Users/aryak/Downloads/learn-spring-batch/src/main/resources/employees.xlsx"));

        // 2 different options

        // 1. custom row mapper
        RowMapper<Employee> employeeRowMapper = new EmployeeRowMapper();


        // 2.
        BeanWrapperRowMapper<Employee> beanWrapperRowMapper = new BeanWrapperRowMapper<>();
        beanWrapperRowMapper.setTargetType(Employee.class);
        employeePoiItemReader.setRowMapper(beanWrapperRowMapper);
        return employeePoiItemReader;
    }

    @Bean
    public JsonItemReader<Employee> jsonItemReader() {
        return new JsonItemReaderBuilder<Employee>()
                .name("jsonReader")
                .jsonObjectReader(new JacksonJsonObjectReader<>(Employee.class))
                .resource(new FileSystemResource("/Users/aryak/Downloads/learn-spring-batch/src/main/resources/employees.json"))
                .build();
    }
}
