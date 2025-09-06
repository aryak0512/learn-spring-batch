package com.aryak.learn.rowmappers;

import com.aryak.learn.model.Employee;
import org.springframework.batch.extensions.excel.RowMapper;
import org.springframework.batch.extensions.excel.support.rowset.RowSet;

public class EmployeeRowMapper implements RowMapper<Employee> {
    @Override
    public Employee mapRow(final RowSet rowSet) throws Exception {

        // mapping logic
        String[] currentRow = rowSet.getCurrentRow();
        long id = Long.parseLong(currentRow[0]);
        String name = currentRow[1];
        String location = currentRow[2];
        String email = currentRow[3];
        return new Employee(id, name, location, email);
    }
}
