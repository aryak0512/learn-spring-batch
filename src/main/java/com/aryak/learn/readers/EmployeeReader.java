package com.aryak.learn.readers;

import com.aryak.learn.model.Employee;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Component
public class EmployeeReader implements ItemReader<Employee> {

    private final List<String> lines;
    // have the reference to the data source - file
    private int count = 0;

    public EmployeeReader() throws IOException {
        lines = Files.readAllLines(Path.of("/Users/aryak/Downloads/learn-spring-batch/src/main/resources/employees.csv"));
    }

    @Override
    public Employee read() throws Exception {

        if ( count < lines.size() ) {
            // keep reading
            String[] columnValues = lines.get(count++).split(",");
            long id = Long.parseLong(columnValues[0]);
            String name = columnValues[1];
            String location = columnValues[2];
            String email = columnValues[3];
            return new Employee(id, name, location, email);
        }

        return null; //EOF
    }
}
