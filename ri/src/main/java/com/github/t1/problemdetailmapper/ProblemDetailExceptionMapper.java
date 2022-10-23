package com.github.t1.problemdetailmapper;

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
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Maps exceptions to a response with a body containing problem details
 * as specified in <a href="https://tools.ietf.org/html/rfc7807">rfc-7807</a>
 */
@Slf4j
@Provider
public class ProblemDetailExceptionMapper implements ExceptionMapper<Exception> {
    private static final List<String> UNWRAP = asList("javax.ejb.EJBException", "java.lang.IllegalStateException",
        "java.util.concurrent.CompletionException");

    @Context
    HttpHeaders requestHeaders;

    @Override public Response toResponse(Exception exception) {
        Response response = (exception instanceof WebApplicationException)
            ? ((WebApplicationException) exception).getResponse() : null;
        if (response != null && response.hasEntity()) {
            return response;
        }

        while (UNWRAP.contains(exception.getClass().getName()) && exception.getCause() instanceof Exception /* implies not null */) {
            exception = (Exception) exception.getCause();
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
