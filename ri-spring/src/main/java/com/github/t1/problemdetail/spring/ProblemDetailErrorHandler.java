package com.github.t1.problemdetail.spring;

import com.github.t1.problemdetail.ri.lib.ProblemDetailJsonToExceptionBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.IOException;

import static com.github.t1.problemdetail.Constants.PROBLEM_DETAIL_JSON;

/**
 * This is the client side tool to turn problem details into exceptions. To use it, set
 * a new instance of this class as an error handler on your `RestTemplate`:
 * <pre><code>
 * template.setErrorHandler(new ProblemDetailErrorHandler());
 * </code></pre>
 */
public class ProblemDetailErrorHandler extends DefaultResponseErrorHandler {
    private static final MediaType PROBLEM_DETAIL_JSON_MEDIA_TYPE = MediaType.parseMediaType(PROBLEM_DETAIL_JSON);

    @Override public void handleError(ClientHttpResponse response) throws IOException {
        // TODO XML: https://github.com/t1/problem-details/issues/6
        if (PROBLEM_DETAIL_JSON_MEDIA_TYPE.isCompatibleWith(response.getHeaders().getContentType())) {
            new ProblemDetailJsonToExceptionBuilder(response.getBody()).trigger();
        } else {
            super.handleError(response);
        }
    }
}
