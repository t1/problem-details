package com.github.t1.problemdetails.jaxrs;

import javax.annotation.Priority;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

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
