package com.github.t1.problemdetail.ri;

import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.InputStream;

import static com.github.t1.problemdetail.ri.ProblemDetailClientResponseFilter.PROBLEM_DETAIL_JSON_TYPE;

@Provider
public class ProblemDetailResponseExceptionMapper implements ResponseExceptionMapper<Throwable> {
    @Override public boolean handles(int status, MultivaluedMap<String, Object> headers) {
        return status >= 400 && isProblemDetail(headers.getFirst("Content-Type"));
    }

    private boolean isProblemDetail(Object contentType) {
        // TODO also support XML problem detail bodies https://github.com/t1/problem-details/issues/6
        return contentType == null // TODO fix after release of RESTEASY-2460 #2249 4.5.0: != &&
            || PROBLEM_DETAIL_JSON_TYPE.isCompatible(MediaType.valueOf(contentType.toString()));
    }

    @Override public Throwable toThrowable(Response response) {
        if (!response.hasEntity()) {
            // return null should skip this ResponseExceptionMapper, but RestEasy MP throws an NPE
            return new IllegalStateException("received problem detail without entity");
        }
        try {
            return new JaxRsProblemDetailJsonToExceptionBuilder(response.readEntity(InputStream.class)).build();
        } catch (RuntimeException e) {
            throw new RuntimeException("can't read json body from " + response.getStatus() + " " + response.getStatusInfo()
                + " response" + stringBody(response));
        }
    }

    private String stringBody(Response response) {
        if (response.hasEntity()) {
            String entity = response.readEntity(String.class);
            if (!entity.isEmpty()) {
                return ":\n" + entity;
            }
        }
        return "";
    }
}
