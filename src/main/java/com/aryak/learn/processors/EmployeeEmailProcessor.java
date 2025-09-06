package com.aryak.learn.processors;

import com.aryak.learn.model.Employee;
import com.aryak.learn.model.EmployeeProcessed;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class EmployeeEmailProcessor implements ItemProcessor<Employee, EmployeeProcessed> {

    @Override
    public EmployeeProcessed process(final Employee employee) throws Exception {
        EmployeeProcessed employeeProcessed = new EmployeeProcessed();
        employeeProcessed.setId(employee.getId());
        employeeProcessed.setName(employee.getName());
        employeeProcessed.setLocation(employee.getLocation());
        employeeProcessed.setEmail(transformEmail(employee.getEmail()));
        return employeeProcessed;
    }

    private String transformEmail(final String email) {
        // migrate the domain from gmail to yahoo & convert to lowercase
        return email.toLowerCase().replace("gmail", "yahoo");
    }
}
