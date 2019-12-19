package com.github.t1.problemdetail.ri;

import com.github.t1.problemdetail.ri.lib.ProblemDetails;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Maps exceptions to a response with a body containing problem details
 * as specified in https://tools.ietf.org/html/rfc7807
 */
@Slf4j
@Provider
public class ProblemDetailExceptionMapper implements ExceptionMapper<Exception> {
    @Context
    HttpHeaders requestHeaders;

    @Override public Response toResponse(Exception exception) {
        Response response = (exception instanceof WebApplicationException)
            ? ((WebApplicationException) exception).getResponse() : null;
        if (response != null && response.hasEntity()) {
            return response;
        }

        ProblemDetails problemDetail = new ProblemDetails(exception) {
            @Override protected StatusType fallbackStatus() {
                return (response != null) ? response.getStatusInfo() : super.fallbackStatus();
            }

            @Override protected boolean hasDefaultMessage() {
                return exception.getMessage() != null
                    && exception.getMessage().equals("HTTP " + getStatus().getStatusCode()
                    + " " + getStatus().getReasonPhrase());
            }

            @Override protected String findMediaTypeSubtype() {
                for (MediaType accept : requestHeaders.getAcceptableMediaTypes()) {
                    if ("application".equals(accept.getType())) {
                        return accept.getSubtype();
                    }
                }
                return "json";
            }
        };

        return Response
            .status(problemDetail.getStatus())
            .entity(problemDetail.getBody())
            .header("Content-Type", problemDetail.getMediaType())
            .build();
    }
}
