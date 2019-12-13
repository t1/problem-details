package com.github.t1.problemdetail.spring;

import com.github.t1.problemdetail.ri.lib.ProblemDetailJsonToExceptionBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.IOException;

import static com.github.t1.problemdetail.Constants.PROBLEM_DETAIL_JSON;

public class ProblemDetailErrorHandler extends DefaultResponseErrorHandler {
    private static final MediaType PROBLEM_DETAIL_JSON_MEDIA_TYPE = MediaType.parseMediaType(PROBLEM_DETAIL_JSON);

    @Override public void handleError(ClientHttpResponse response) throws IOException {
        // TODO XML
        if (PROBLEM_DETAIL_JSON_MEDIA_TYPE.isCompatibleWith(response.getHeaders().getContentType())) {
            new ProblemDetailJsonToExceptionBuilder(response.getBody()).trigger();
        } else {
            super.handleError(response);
        }
    }
}
