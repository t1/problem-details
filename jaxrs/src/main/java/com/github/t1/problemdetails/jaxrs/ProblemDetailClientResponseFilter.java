package com.github.t1.problemdetails.jaxrs;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.Provider;

import static org.eclipse.microprofile.problemdetails.Constants.PROBLEM_DETAIL_JSON;

@Provider
public class ProblemDetailClientResponseFilter implements ClientResponseFilter {
    public static final MediaType PROBLEM_DETAIL_JSON_TYPE = MediaType.valueOf(PROBLEM_DETAIL_JSON);

    @Override public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) {
        // TODO XML: https://github.com/t1/problem-details/issues/6
        if (PROBLEM_DETAIL_JSON_TYPE.isCompatible(responseContext.getMediaType()) && responseContext.hasEntity()) {
            new JaxRsProblemDetailJsonToExceptionBuilder(responseContext.getEntityStream())
                .trigger();
        }
    }
}
