package com.github.t1.problemdetaildemoapp;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.ServiceUnavailableException;
import jakarta.ws.rs.core.Response;

import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;

@Path("/standard")
public class StandardExceptionBoundary {
    @Path("/plain-bad-request")
    @POST public void plainBadRequest() {
        throw new BadRequestException();
    }

    @Path("/bad-request-with-message")
    @POST public void badRequestWithMessage() {
        throw new BadRequestException("some message");
    }

    @Path("/bad-request-with-text-response")
    @POST public void badRequestWithResponse() {
        throw new BadRequestException(Response.status(BAD_REQUEST)
            .type(TEXT_PLAIN_TYPE).entity("the body").build());
    }

    @Path("/plain-service-unavailable")
    @POST public void plainServiceUnavailable() {
        throw new ServiceUnavailableException();
    }

    @Path("/illegal-argument-without-message")
    @POST public void illegalArgumentWithoutMessage() {
        throw new IllegalArgumentException();
    }

    @Path("/illegal-argument-with-message")
    @POST public void illegalArgumentWithMessage() {
        throw new IllegalArgumentException("some message");
    }

    @Path("/npe-without-message")
    @POST public void npeWithoutMessage() {
        throw new NullPointerException();
    }

    @Path("/npe-with-message")
    @POST public void npeWithMessage() {
        throw new NullPointerException("some message");
    }
}
