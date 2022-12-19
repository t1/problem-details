package com.github.t1.problemdetailmapper;

import com.github.t1.problemdetail.ri.lib.ProblemDetailJsonToExceptionBuilder;
import lombok.extern.slf4j.Slf4j;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;
import jakarta.ws.rs.ext.Provider;

import static com.github.t1.problemdetail.Constants.PROBLEM_DETAIL_JSON_TYPE;

@Slf4j
@Provider
public class ProblemDetailHandler implements ClientResponseFilter {
    @Override public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) {
        // TODO XML
        if (PROBLEM_DETAIL_JSON_TYPE.isCompatible(responseContext.getMediaType())) {
            new ProblemDetailJsonToExceptionBuilder(responseContext.getEntityStream())
                .trigger();
        }
    }
}
