package com.github.t1.problemdetail.ri;

import com.github.t1.problemdetail.ri.lib.ProblemDetailJsonToExceptionBuilder;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.InputStream;

import static com.github.t1.problemdetail.Constants.PROBLEM_DETAIL_JSON_TYPE;

public class ProblemDetailResponseExceptionMapper implements ResponseExceptionMapper<Throwable> {
    @Override public Throwable toThrowable(Response response) {
        return new ProblemDetailJsonToExceptionBuilder(response.readEntity(InputStream.class)).build();
    }

    @Override public boolean handles(int status, MultivaluedMap<String, Object> headers) {
        return status >= 400 && isProblemDetail(headers.getFirst("Content-Type"));
    }

    private boolean isProblemDetail(Object contentType) {
        return contentType == null // TODO there's a TODO in RestEasy PartialResponse#getMetadata(). Should be != &&
            || PROBLEM_DETAIL_JSON_TYPE.isCompatible(MediaType.valueOf(contentType.toString()));
    }
}
