package com.github.t1.problemdetail.ri.lib;

import lombok.Data;

import javax.xml.bind.annotation.XmlType;
import java.net.URI;

/**
 * Http response body containing problem details as specified in
 * <a href="https://tools.ietf.org/html/rfc7807">RFC-7807</a>
 */
@Data
@XmlType(name = "problem", propOrder = {"type", "title", "status", "detail", "instance"})
public class ProblemDetail {
    @Override public String toString() {
        return "ProblemDetail:" + type + ":" + title + ":" + status + ":" + detail + ":" + instance;
    }

    private URI type;
    private String title;
    private Integer status;
    private String detail;
    private URI instance;
}
