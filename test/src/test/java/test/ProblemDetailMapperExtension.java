package test;

import com.github.t1.problemdetail.ProblemDetail;
import com.github.t1.problemdetailmapper.ProblemDetailJsonMessageBodyReader;
import com.github.t1.problemdetailmapper.ProblemDetailXmlMessageBodyReader;
import com.github.t1.testcontainers.jee.JeeContainer;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.BDDAssertions;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.net.URI;
import java.util.function.Consumer;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

class ProblemDetailMapperExtension implements Extension, BeforeAllCallback {
    private static URI BASE_URI = null;

    /**
     * Stopping is done by the ryuk container... see
     * https://www.testcontainers.org/test_framework_integration/manual_lifecycle_control/#singleton-containers
     */
    @Override public void beforeAll(ExtensionContext context) {
        if (System.getProperty("testcontainer-running") != null) {
            BASE_URI = URI.create(System.getProperty("testcontainer-running"));
        } else if (BASE_URI == null) {
            JeeContainer container = JeeContainer.create()
                .withDeployment("target/jax-rs-problem-detail-test.war");
            container.start();
            BASE_URI = container.baseUri();
        }
    }

    public Response post(String path) {
        return post(path, null);
    }

    public Response post(String path, Entity<?> entity) {
        return target(path).request(APPLICATION_JSON_TYPE).post(entity);
    }

    public WebTarget target(String path) {
        return target().path(path);
    }

    private static final Client CLIENT = ClientBuilder.newClient()
        .register(ProblemDetailJsonMessageBodyReader.class)
        .register(ProblemDetailXmlMessageBodyReader.class);

    private WebTarget target() {
        return CLIENT.target(BASE_URI);
    }

    public static ProblemDetailAssert<ProblemDetail> then(Response response) {
        return then(response, ProblemDetail.class);
    }

    public static <T extends ProblemDetail> ProblemDetailAssert<T> then(Response response, Class<T> type) {
        BDDAssertions.then(response.hasEntity()).describedAs("response has entity").isTrue();
        T problemDetail = response.readEntity(type);
        return new ProblemDetailAssert<>(response, problemDetail);
    }

    @RequiredArgsConstructor
    public static class ProblemDetailAssert<T extends ProblemDetail> {
        private final Response response;
        private final T problemDetail;

        public ProblemDetailAssert<T> hasStatus(Status status) {
            BDDAssertions.then(response.getStatusInfo()).describedAs("response status")
                .isEqualTo(status);
            BDDAssertions.then(problemDetail.getStatus()).describedAs("problem-detail.status")
                .isEqualTo(status.getStatusCode());
            return this;
        }

        public ProblemDetailAssert<T> hasMediaType(MediaType mediaType) {
            BDDAssertions.then(response.getMediaType()).describedAs("response content type")
                .isEqualTo(mediaType);
            return this;
        }

        public ProblemDetailAssert<T> hasType(String type) {
            BDDAssertions.then(problemDetail.getType()).describedAs("problem-detail.type")
                .isEqualTo(URI.create(type));
            return this;
        }

        public ProblemDetailAssert<T> hasTitle(String title) {
            BDDAssertions.then(problemDetail.getTitle()).describedAs("problem-detail.title")
                .isEqualTo(title);
            return this;
        }

        public ProblemDetailAssert<T> hasDetail(String detail) {
            BDDAssertions.then(problemDetail.getDetail()).describedAs("problem-detail.detail")
                .isEqualTo(detail);
            return this;
        }

        public ProblemDetailAssert<T> hasUuidInstance() {
            BDDAssertions.then(problemDetail.getInstance()).describedAs("problem-detail.instance")
                .has(new Condition<>(instance -> instance.toString().startsWith("urn:uuid:"), "some uuid urn"));
            return this;
        }

        public ProblemDetailAssert<T> hasInstance(URI instance) {
            BDDAssertions.then(problemDetail.getInstance()).describedAs("problem-detail.instance")
                .isEqualTo(instance);
            return this;
        }

        public void checkExtensions(Consumer<T> consumer) {
            consumer.accept(problemDetail);
        }
    }
}
