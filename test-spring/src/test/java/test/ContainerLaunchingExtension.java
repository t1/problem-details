package test;

import com.github.t1.problemdetail.ProblemDetail;
import com.github.t1.problemdetaildemoapp.Application;
import lombok.SneakyThrows;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.function.Consumer;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_XML;
import static org.springframework.http.MediaType.TEXT_PLAIN;

class ContainerLaunchingExtension implements Extension, BeforeAllCallback {
    private static URI BASE_URI = null;

    @SneakyThrows(InterruptedException.class)
    @Override public void beforeAll(ExtensionContext context) {
        if (BASE_URI == null) {
            Application.main();
            BASE_URI = URI.create("http://localhost:8080/");
            waitUntilHealthy();
        }
    }

    private void waitUntilHealthy() throws InterruptedException {
        RestTemplate rest = createRestTemplate(TEXT_PLAIN);
        long start = System.currentTimeMillis();
        while (true) {
            Thread.sleep(300);
            ResponseEntity<String> response = rest.getForEntity(BASE_URI + "/actuator/health", String.class);
            if (OK.equals(response.getStatusCode()) && response.getBody() != null
                && response.getBody().contains("\"status\":\"UP\"")) {
                break;
            }
            if (System.currentTimeMillis() - start > 30_000) {
                throw new IllegalStateException("spring didn't start in 30 secs");
            }
        }
    }

    public static <T extends ProblemDetail> ProblemDetailAssert<T> testPost(String path, Class<T> type) {
        return then(post(createRestTemplate(), path, type));
    }

    public static ProblemDetailAssert<ProblemDetail> testPost(String path) {
        return then(post(createRestTemplate(), path, ProblemDetail.class));
    }

    public static ProblemDetailAssert<ProblemDetail> testPost(RestTemplate restTemplate, String path) {
        return then(post(restTemplate, path, ProblemDetail.class));
    }

    public static ProblemDetailAssert<ProblemDetail> testPost(String path, MediaType accept) {
        RestTemplate restTemplate = createRestTemplate(accept);
        return then(post(restTemplate, path, ProblemDetail.class));
    }

    public static ProblemDetailAssert<ProblemDetail> testPost(String path, MediaType accept1, MediaType accept2) {
        RestTemplate template = new RestTemplate();
        template.setMessageConverters(asList(messageConverterFor(accept1), messageConverterFor(accept2)));
        template.setErrorHandler(new AcceptAllResponseErrorHandler());
        return then(post(template, path, ProblemDetail.class));
    }

    public static <T> ResponseAssert<T> testPost(String path, MediaType accept, Class<T> type) {
        RestTemplate restTemplate = createRestTemplate(accept);
        return new ResponseAssert<>(post(restTemplate, path, type));
    }

    private static <T> ResponseEntity<T> post(RestTemplate restTemplate, String path, Class<T> type) {
        return restTemplate.postForEntity(BASE_URI + path, null, type);
    }

    public static RestTemplate createRestTemplate() { return createRestTemplate(APPLICATION_JSON); }

    private static RestTemplate createRestTemplate(MediaType accept) {
        RestTemplate template = new RestTemplate();
        template.setMessageConverters(singletonList(messageConverterFor(accept)));
        template.setErrorHandler(new AcceptAllResponseErrorHandler());
        return template;
    }

    private static HttpMessageConverter<?> messageConverterFor(MediaType mediaType) {
        if (APPLICATION_JSON.equals(mediaType))
            return new MappingJackson2HttpMessageConverter();
        else if (APPLICATION_XML.equals(mediaType))
            return new Jaxb2RootElementHttpMessageConverter();
        else if (TEXT_PLAIN.equals(mediaType))
            return new StringHttpMessageConverter();
        else
            throw new IllegalArgumentException("unsupported media type " + mediaType);
    }

    private static class AcceptAllResponseErrorHandler implements ResponseErrorHandler {
        @Override public boolean hasError(@NonNull ClientHttpResponse response) { return false; }

        @Override public void handleError(@NonNull ClientHttpResponse response) {
            throw new UnsupportedOperationException();
        }
    }


    public static <T extends ProblemDetail> ProblemDetailAssert<T> then(ResponseEntity<T> response) {
        return new ProblemDetailAssert<>(response);
    }

    static class ProblemDetailAssert<T extends ProblemDetail> extends ResponseAssert<T> {

        public ProblemDetailAssert(ResponseEntity<T> response) { super(response); }

        @Override public ProblemDetailAssert<T> hasStatus(HttpStatus status) {
            super.hasStatus(status);
            return this;
        }

        @Override public ProblemDetailAssert<T> hasContentType(String contentType) {
            super.hasContentType(contentType);
            return this;
        }

        @Override public ProblemDetailAssert<T> hasContentType(MediaType contentType) {
            super.hasContentType(contentType);
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

    public static class ResponseAssert<T> {
        protected final ResponseEntity<T> response;
        protected final T body;

        ResponseAssert(ResponseEntity<T> response) {
            this.response = response;
            this.body = response.getBody();
        }

        public ResponseAssert<T> hasStatus(HttpStatus status) {
            assertThat(response.getStatusCode()).isEqualTo(status);
            return this;
        }

        public ResponseAssert<T> hasContentType(String contentType) { return hasContentType(MediaType.valueOf(contentType)); }

        public ResponseAssert<T> hasContentType(MediaType contentType) {
            assertThat(response.getHeaders().getContentType()).isEqualTo(contentType);
            return this;
        }

        @SuppressWarnings("UnusedReturnValue") public ResponseAssert<T> hasBody(T body) {
            assertThat(this.body).isEqualTo(body);
            return this;
        }
    }
}
