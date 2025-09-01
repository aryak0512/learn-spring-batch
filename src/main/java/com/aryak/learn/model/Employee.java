package com.aryak.learn.model;

public record Employee(
        Long id,
        String name,
        String location,
        String email
) {
}
