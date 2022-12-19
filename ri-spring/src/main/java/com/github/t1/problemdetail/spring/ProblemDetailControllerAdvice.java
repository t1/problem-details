package com.github.t1.problemdetail.spring;

import com.github.t1.problemdetail.ri.lib.ProblemDetails;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.Response.StatusType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientResponseException;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * Maps exceptions to a response with a body containing problem details
 * as specified in <a href="https://tools.ietf.org/html/rfc7807">rfc-7807</a>
 */
@Slf4j
@ControllerAdvice
public class ProblemDetailControllerAdvice {
    @ExceptionHandler(Exception.class)
    @ResponseBody ResponseEntity<Object>
    toProblemDetail(HttpServletRequest request, Exception exception) {
        log.debug("handle error", exception);
        ProblemDetails problemDetail = new ProblemDetails(exception) {
            @Override protected Object buildBody() {
                if (exception instanceof RestClientResponseException) {
                    byte[] body = ((RestClientResponseException) exception).getResponseBodyAsByteArray();
                    if (body.length > 0) {
                        return body;
                    }
                }
                return super.buildBody();
            }

            @Override protected String buildResponseMediaType() {
                if (exception instanceof RestClientResponseException) {
                    HttpHeaders responseHeaders = ((RestClientResponseException) exception).getResponseHeaders();
                    if (responseHeaders != null) {
                        MediaType contentType = responseHeaders.getContentType();
                        if (contentType != null) {
                            return contentType.toString();
                        }
                    }
                }

                return super.buildResponseMediaType();
            }

            @Override protected String findMediaTypeSubtype() {
                Enumeration<String> i = request.getHeaders("Accept");
                while (i.hasMoreElements()) {
                    for (MediaType accept : MediaType.parseMediaTypes(i.nextElement())) {
                        if ("application".equals(accept.getType())) {
                            return accept.getSubtype();
                        }
                    }
                }
                return "json";
            }

            @Override protected StatusType buildStatus() {
                if (exception instanceof HttpStatusCodeException) {
                    return Status.fromStatusCode(((HttpStatusCodeException) exception).getStatusCode().value());
                } else {
                    return super.buildStatus();
                }
            }

            public int getStatusCode() {
                if (exception instanceof HttpStatusCodeException) {
                    return ((HttpStatusCodeException) exception).getStatusCode().value();
                } else {
                    return super.getStatusCode();
                }
            }

            @Override protected boolean hasDefaultMessage() {
                if (exception instanceof HttpStatusCodeException) {
                    HttpStatus status = ((HttpStatusCodeException) exception).getStatusCode();
                    return (status.value() + " " + status.getReasonPhrase()).equals(exception.getMessage());
                } else {
                    return false;
                }
            }
        };

        return ResponseEntity.status(problemDetail.getStatusCode())
            .contentType(MediaType.valueOf(problemDetail.getMediaType()))
            .body(problemDetail.getBody());
    }
}
