package com.aryak.learn.model;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class EmployeeProcessed {

    private Long id;
    private String name;
    private String location;
    private String email;

}
