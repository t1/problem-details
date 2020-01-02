package com.github.t1.problemdetaildemoapp;

import lombok.Data;

import java.net.URI;

@Data
public class ProblemDetail {
    private URI type;
    private String title;
    private String detail;
    private Integer status;
    private URI instance;
}
