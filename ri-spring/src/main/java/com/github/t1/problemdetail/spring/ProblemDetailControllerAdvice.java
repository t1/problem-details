package com.github.t1.problemdetail.spring;

import com.github.t1.problemdetail.ri.lib.ProblemDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.NestedServletException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;
import java.util.Enumeration;

/**
 * The server side tool to convert an exception into a response with a problem detail body
 * as specified in https://tools.ietf.org/html/rfc7807
 */
@Slf4j
@Order(0)
@ControllerAdvice
public class ProblemDetailControllerAdvice {
    @ExceptionHandler(Exception.class)
    ResponseEntity<Object> toProblemDetail(HttpServletRequest request, Exception exception) {
        // some exceptions are nested, e.g. an OOM error
        if (exception instanceof NestedServletException) {
            // we need to handle the cause or rethrow them
            Throwable cause = exception.getCause();
            if (cause instanceof Error)
                throw (Error) cause;
            exception = (Exception) cause; // other subclasses should not exist... and how should we handle them?
        }

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

            @Override protected String buildMediaType() {
                if (exception instanceof RestClientResponseException) {
                    HttpHeaders responseHeaders = ((RestClientResponseException) exception).getResponseHeaders();
                    if (responseHeaders != null) {
                        MediaType contentType = responseHeaders.getContentType();
                        if (contentType != null) {
                            return contentType.toString();
                        }
                    }
                }

                return super.buildMediaType();
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

            @Override protected boolean hasDefaultMessage() {
                if (exception instanceof HttpStatusCodeException) {
                    HttpStatus status = ((HttpStatusCodeException) exception).getStatusCode();
                    return exception.getMessage().equals(status.value() + " " + status.getReasonPhrase());
                } else {
                    return false;
                }
            }
        };

        return ResponseEntity.status(problemDetail.getStatus().getStatusCode())
            .contentType(MediaType.valueOf(problemDetail.getMediaType()))
            .body(problemDetail.getBody());
    }
}
