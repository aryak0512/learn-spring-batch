package com.aryak.learn.config;

import com.aryak.learn.model.Employee;
import com.aryak.learn.model.EmployeeProcessed;
import com.aryak.learn.processors.EmployeeEmailProcessor;
import com.aryak.learn.processors.EmployeeLocationProcessor;
import com.aryak.learn.readers.EmployeeReader;
import com.aryak.learn.rowmappers.EmployeeRowMapper;
import com.aryak.learn.utils.S3Utils;
import com.aryak.learn.writers.EmployeeWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.extensions.excel.RowMapper;
import org.springframework.batch.extensions.excel.mapping.BeanWrapperRowMapper;
import org.springframework.batch.extensions.excel.poi.PoiItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.IOException;
import java.util.List;

@Configuration
public class EmployeeJobConfig {

    private final EmployeeReader employeeReader;
    private final EmployeeWriter employeeWriter;
    private final JobRepository jobRepository;
    private final EmployeeLocationProcessor employeeLocationProcessor;
    private final PlatformTransactionManager platformTransactionManager;
    private final S3Utils s3Utils;

    public EmployeeJobConfig(final EmployeeReader employeeReader,
                             final EmployeeWriter employeeWriter,
                             final JobRepository jobRepository,
                             final EmployeeLocationProcessor employeeLocationProcessor,
                             final PlatformTransactionManager platformTransactionManager, final S3Utils s3Utils) {
        this.employeeReader = employeeReader;
        this.employeeWriter = employeeWriter;
        this.jobRepository = jobRepository;
        this.employeeLocationProcessor = employeeLocationProcessor;
        this.platformTransactionManager = platformTransactionManager;
        this.s3Utils = s3Utils;
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
                             JsonItemReader<Employee> jsonItemReader,
                             FlatFileItemReader<Employee> s3Reader) {
        return new StepBuilder("employeeStep", jobRepository)
                .<Employee, EmployeeProcessed>chunk(10, platformTransactionManager)
                .reader(employeeFlatFileItemReader)
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
    @StepScope
    public FlatFileItemReader<Employee> employeeFlatFileItemReader(@Value("#{jobParameters['filePath']}") String filePath) {

        String defaultPath = "/Users/aryak/Downloads/learn-spring-batch/src/main/resources/employees.csv";
        FlatFileItemReader<Employee> reader = new FlatFileItemReader<>();
        reader.setResource(new FileSystemResource(filePath == null ? defaultPath : filePath));
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

    //@Bean
    public FlatFileItemReader<Employee> s3Reader() throws IOException {

        var responseStream = s3Utils.getResponseStream("<your-bucket>", "<your-filename>");

        // To make it restartable, copy into memory (or temp file)
        byte[] fileBytes = responseStream.readAllBytes();
        return new FlatFileItemReaderBuilder<Employee>()
                .name("s3FileReader")
                .resource(new org.springframework.core.io.ByteArrayResource(fileBytes))
                .delimited()
                .names("id", "name", "location", "email")
                .targetType(Employee.class)
                .build();
    }

}
