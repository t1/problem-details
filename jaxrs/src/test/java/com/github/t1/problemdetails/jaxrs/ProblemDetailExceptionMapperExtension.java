package com.github.t1.problemdetails.jaxrs;

import org.jboss.resteasy.specimpl.ResteasyHttpHeaders;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.Response;

public class ProblemDetailExceptionMapperExtension implements Extension, BeforeEachCallback {
    private MultivaluedHashMap<String, String> requestHeaders = new MultivaluedHashMap<>();
    private ProblemDetailExceptionMapper mapper = new ProblemDetailExceptionMapper();

    @Override public void beforeEach(ExtensionContext context) {
        mapper.requestHeaders = new ResteasyHttpHeaders(requestHeaders);
    }

    public void accepting(String value) { requestHeaders.putSingle("Accept", value); }

    public Response toResponse(Throwable throwable) { return mapper.toResponse(throwable); }
}
