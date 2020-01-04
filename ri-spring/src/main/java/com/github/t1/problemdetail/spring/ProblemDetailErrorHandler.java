package com.github.t1.problemdetail.spring;

import com.github.t1.problemdetail.ri.lib.ProblemDetailJsonToExceptionBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.io.InputStream;

import static com.github.t1.problemdetail.Constants.PROBLEM_DETAIL_JSON;
import static java.nio.charset.StandardCharsets.UTF_8;

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
            new SpringBootProblemDetailJsonToExceptionBuilder(response.getBody()).trigger();
        } else {
            super.handleError(response);
        }
    }

    private static class SpringBootProblemDetailJsonToExceptionBuilder extends ProblemDetailJsonToExceptionBuilder {
        public SpringBootProblemDetailJsonToExceptionBuilder(InputStream body) { super(body); }

        @Override public Throwable build() {
            if (type == null)
                return createSpringException();
            return super.build();
        }

        private HttpClientErrorException createSpringException() {
            HttpStatus status = HttpStatus.valueOf(getStatusCode());
            //noinspection ConstantConditions // headers is actually Nullable, only the annotation is missing
            return HttpClientErrorException.create(getDetail(), status, status.getReasonPhrase(), null, body.toString().getBytes(UTF_8), UTF_8);
        }
    }
}
