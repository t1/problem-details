package test;

import com.github.t1.problemdetail.ProblemDetail;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.function.Consumer;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class TestTools {
    public static ResponseEntity<ProblemDetail> post(String path) {
        return post(createRestTemplate(), path);
    }

    public static ResponseEntity<ProblemDetail> post(RestTemplate restTemplate, String path) {
        return post(restTemplate, path, ProblemDetail.class);
    }

    public static <T> ResponseEntity<T> post(String path, Class<T> type) {
        return post(createRestTemplate(), path, type);
    }

    public static <T> ResponseEntity<T> post(RestTemplate restTemplate, String path, Class<T> type) {
        return restTemplate.postForEntity("http://localhost:8080/" + path, null, type);
    }

    public static RestTemplate createRestTemplate() {
        RestTemplate template = new RestTemplate();
        template.setMessageConverters(singletonList(new MappingJackson2HttpMessageConverter()));
        template.setErrorHandler(new AcceptAllResponseErrorHandler());
        return template;
    }

    public static class AcceptAllResponseErrorHandler implements ResponseErrorHandler {
        @Override public boolean hasError(@NonNull ClientHttpResponse response) { return false; }

        @Override public void handleError(@NonNull ClientHttpResponse response) {
            throw new UnsupportedOperationException();
        }
    }


    public static <T extends ProblemDetail> ProblemDetailAssert<T> then(ResponseEntity<T> response) {
        return new ProblemDetailAssert<>(response);
    }

    static class ProblemDetailAssert<T extends ProblemDetail> {
        private final ResponseEntity<T> response;
        private final T body;

        ProblemDetailAssert(ResponseEntity<T> response) {
            this.response = response;
            this.body = response.getBody();
        }

        public ProblemDetailAssert<T> hasStatus(HttpStatus status) {
            assertThat(response.getStatusCode()).isEqualTo(status);
            return this;
        }

        public ProblemDetailAssert<T> hasMediaType(String mediaType) { return hasMediaType(MediaType.valueOf(mediaType)); }

        public ProblemDetailAssert<T> hasMediaType(MediaType mediaType) {
            assertThat(response.getHeaders().getContentType()).isEqualTo(mediaType);
            return this;
        }

        public ProblemDetailAssert<T> hasType(String type) { return hasType(URI.create(type)); }

        public ProblemDetailAssert<T> hasType(URI type) {
            assertThat(body).isNotNull();
            assertThat(body.getType()).isEqualTo(type);
            return this;
        }

        public ProblemDetailAssert<T> hasTitle(String title) {
            assertThat(body).isNotNull();
            assertThat(body.getTitle()).isEqualTo(title);
            return this;
        }

        public ProblemDetailAssert<T> hasDetail(String detail) {
            assertThat(body).isNotNull();
            assertThat(body.getDetail()).isEqualTo(detail);
            return this;
        }

        public ProblemDetailAssert<T> hasUuidInstance() {
            assertThat(body).isNotNull();
            assertThat(body.getInstance()).isNotNull();
            assertThat(body.getInstance().toString()).startsWith("urn:uuid:");
            return this;
        }

        public void checkExtensions(Consumer<T> consumer) {
            consumer.accept(body);
        }
    }
}
