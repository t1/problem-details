package com.github.t1.problemdetail.ri;

import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import static org.eclipse.microprofile.problemdetails.Constants.PROBLEM_DETAIL_JSON;

@Slf4j
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
