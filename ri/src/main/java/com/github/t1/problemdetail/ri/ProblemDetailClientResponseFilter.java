package com.github.t1.problemdetail.ri;

import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.ext.Provider;

import static com.github.t1.problemdetail.Constants.PROBLEM_DETAIL_JSON_TYPE;

@Slf4j
@Provider
public class ProblemDetailClientResponseFilter implements ClientResponseFilter {
    @Override public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) {
        // TODO XML: https://github.com/t1/problem-details/issues/6
        if (PROBLEM_DETAIL_JSON_TYPE.isCompatible(responseContext.getMediaType()) && responseContext.hasEntity()) {
            new JaxRsProblemDetailJsonToExceptionBuilder(responseContext.getEntityStream())
                .trigger();
        }
    }
}
