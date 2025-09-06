package com.aryak.learn.processors;

import com.aryak.learn.model.Employee;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class EmployeeLocationProcessor implements ItemProcessor<Employee, Employee> {

    @Override
    public Employee process(final Employee employee) throws Exception {

        if ( !employee.getLocation().equals("Glasgow") ) {
            return employee;
        }

        return null; // skip the record
    }
}
