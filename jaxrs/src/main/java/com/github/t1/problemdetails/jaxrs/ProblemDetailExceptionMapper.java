package com.github.t1.problemdetails.jaxrs;

import jakarta.annotation.Priority;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Maps exceptions to a response with a body containing problem details
 * as specified in https://tools.ietf.org/html/rfc7807
 */
@Provider
@Priority(0)
public class ProblemDetailExceptionMapper implements ExceptionMapper<Throwable> {
    @Context HttpHeaders requestHeaders;

    @Override public Response toResponse(Throwable exception) {
        Response response = (exception instanceof WebApplicationException)
            ? ((WebApplicationException) exception).getResponse() : null;
        if (response != null && response.hasEntity()) {
            return response;
        }

        return new JaxRsProblemDetailBuilder(exception, requestHeaders, response)
            .log()
            .toResponse();
    }
}
