package com.github.t1.problemdetail.ri.lib;

import lombok.Data;

import javax.xml.bind.annotation.XmlType;
import java.net.URI;

/**
 * Http response body containing problem details as specified in
 * <a href="https://tools.ietf.org/html/rfc7807">RFC-7807</a>
 *
 * @deprecated use json-p or xml-dom instead
 */
@Data
@XmlType(name = "problem", propOrder = {"type", "title", "detail", "status", "instance"})
@Deprecated
public class ProblemDetail {
    @Override public String toString() {
        return "ProblemDetail:" + type + ":" + title + ":" + detail + ":" + status + ":" + instance;
    }

    private URI type;
    private String title;
    private String detail;
    private Integer status;
    private URI instance;
}
