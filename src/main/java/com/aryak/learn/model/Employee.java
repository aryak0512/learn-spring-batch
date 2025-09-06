package com.aryak.learn.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Employee {

    private Long id;
    private String name;
    private String location;
    private String email;

}
