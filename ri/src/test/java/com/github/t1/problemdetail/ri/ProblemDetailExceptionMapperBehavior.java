package com.github.t1.problemdetail.ri;

import com.github.t1.problemdetail.Detail;
import com.github.t1.problemdetail.Extension;
import com.github.t1.problemdetail.Instance;
import com.github.t1.problemdetail.Status;
import com.github.t1.problemdetail.Title;
import com.github.t1.problemdetail.Type;
import org.jboss.resteasy.specimpl.ResteasyHttpHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Map;

import static com.github.t1.problemdetail.Constants.PROBLEM_DETAIL_JSON;
import static com.github.t1.problemdetail.Constants.PROBLEM_DETAIL_XML;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.BDDAssertions.then;

class ProblemDetailExceptionMapperBehavior {

    private ProblemDetailExceptionMapper mapper = new ProblemDetailExceptionMapper();
    private MultivaluedHashMap<String, String> requestHeaders;

    @BeforeEach void setUp() {
        requestHeaders = new MultivaluedHashMap<>();
        mapper.requestHeaders = new ResteasyHttpHeaders(requestHeaders);
    }

    @Test void shouldReplyWithJsonTypeByDefault() {
        Response response = mapper.toResponse(new NullPointerException("some message"));

        then(response.getHeaderString("Content-Type")).isEqualTo(PROBLEM_DETAIL_JSON);
    }

    @Test void shouldReplyWithJsonTypeWhenRequested() {
        requestHeaders.putSingle("Accept", APPLICATION_JSON);

        Response response = mapper.toResponse(new NullPointerException("some message"));

        then(response.getHeaderString("Content-Type")).isEqualTo(PROBLEM_DETAIL_JSON);
    }

    @Test void shouldReplyWithXmlTypeWhenRequested() {
        requestHeaders.putSingle("Accept", APPLICATION_XML);

        Response response = mapper.toResponse(new NullPointerException("some message"));

        then(response.getHeaderString("Content-Type")).isEqualTo(PROBLEM_DETAIL_XML);
    }

    @Test void shouldReplyWithYamlTypeWhenRequested() {
        requestHeaders.putSingle("Accept", "application/yaml");

        Response response = mapper.toResponse(new NullPointerException("some message"));

        then(response.getHeaderString("Content-Type")).isEqualTo("application/problem+yaml");
    }

    @Test void shouldMapStandardRuntimeException() {
        Response response = mapper.toResponse(new NullPointerException("some message"));

        then(response.getStatusInfo()).isEqualTo(INTERNAL_SERVER_ERROR);
        then(problemDetailAsMap(response))
            .contains(
                entry("type", URI.create("urn:problem-type:null-pointer")),
                entry("title", "Null Pointer"),
                entry("status", 500),
                entry("detail", "some message"))
            .hasSize(5) // the URN is random
            .containsKey("instance");
    }

    @Test void shouldMapStandardIllegalArgumentException() {
        Response response = mapper.toResponse(new IllegalArgumentException("some message"));

        then(response.getStatusInfo()).isEqualTo(BAD_REQUEST);
        then(problemDetailAsMap(response))
            .contains(
                entry("type", URI.create("urn:problem-type:illegal-argument")),
                entry("title", "Illegal Argument"),
                entry("status", 400),
                entry("detail", "some message"))
            .hasSize(5) // the URN is random
            .containsKey("instance");
    }

    @Test void shouldMapWebApplicationException() {
        Response response = mapper.toResponse(new ForbiddenException("some message"));

        then(response.getStatusInfo()).isEqualTo(FORBIDDEN);
        then(problemDetailAsMap(response))
            .contains(
                entry("type", URI.create("urn:problem-type:forbidden")),
                entry("title", "Forbidden"),
                entry("status", 403),
                entry("detail", "some message"))
            .hasSize(5) // the URN is random
            .containsKey("instance");
    }

    @Test void shouldMapWebApplicationExceptionWithResponse() {
        Response in = Response.serverError().entity("some entity").build();

        Response out = mapper.toResponse(new InternalServerErrorException(in));

        then(out.getStatusInfo()).isEqualTo(INTERNAL_SERVER_ERROR);
        then(out.getEntity()).isSameAs(in.getEntity());
    }

    @Test void shouldMapCustomExceptionWithFields() {
        @Type("some-type")
        @Title("some-title")
        @Status(FORBIDDEN)
        class SomeException extends RuntimeException {
            @Extension private final int f1 = 123;
            @SuppressWarnings("unused") private final int unmapped = 456;
            @Instance private final URI instance = URI.create("https://some.domain/some/path");
            @Detail String detail = "some-detail";
        }

        Response response = mapper.toResponse(new SomeException());

        then(response.getStatusInfo()).isEqualTo(FORBIDDEN);
        then(problemDetailAsMap(response)).containsExactly(
            entry("type", URI.create("some-type")),
            entry("title", "some-title"),
            entry("status", 403),
            entry("detail", "some-detail"),
            entry("instance", URI.create("https://some.domain/some/path")),
            entry("f1", 123)
        );
    }

    @Test void shouldMapCustomExceptionWithMethods() {
        @Type("some-type")
        @Title("some-title")
        @Status(FORBIDDEN)
        class SomeException extends RuntimeException {
            @Extension private int f1() { return 123; }

            @Instance private URI instance() { return URI.create("https://some.domain/some/path"); }

            @Detail String detail() { return "some-detail"; }
        }

        Response response = mapper.toResponse(new SomeException());

        then(response.getStatusInfo()).isEqualTo(FORBIDDEN);
        then(problemDetailAsMap(response)).containsExactly(
            entry("type", URI.create("some-type")),
            entry("title", "some-title"),
            entry("status", 403),
            entry("detail", "some-detail"),
            entry("instance", URI.create("https://some.domain/some/path")),
            entry("f1", 123)
        );
    }

    @Test void shouldMapCustomExceptionWithFailingMethods() {
        @Type("some-type")
        @Title("some-title")
        @Status(FORBIDDEN)
        class SomeException extends RuntimeException {
            @Extension private int f1() { throw new RuntimeException("no f1"); }

            @Instance private URI instance() { throw new IllegalArgumentException("no instance"); }

            @Detail String detail() { throw new NullPointerException(); }
        }

        Response response = mapper.toResponse(new SomeException());

        then(response.getStatusInfo()).isEqualTo(FORBIDDEN);
        then(problemDetailAsMap(response)).containsExactly(
            entry("type", URI.create("some-type")),
            entry("title", "some-title"),
            entry("status", 403),
            entry("detail", "could not invoke SomeException.detail: java.lang.NullPointerException"),
            entry("instance", URI.create("urn:could+not+invoke+SomeException.instance:+java.lang.IllegalArgumentException:+no+instance")),
            entry("f1", "could not invoke SomeException.f1: java.lang.RuntimeException: no f1")
        );
    }

    @Test void shouldMapCustomExceptionWithInvalidInstanceMethod() {
        class SomeException extends RuntimeException {
            @Instance private String instance() { return "evil\nnewlines"; }
        }

        Response response = mapper.toResponse(new SomeException());

        then(response.getStatusInfo()).isEqualTo(INTERNAL_SERVER_ERROR);
        then(problemDetailAsMap(response)).containsExactly(
            entry("type", URI.create("urn:problem-type:some")),
            entry("title", "Some"),
            entry("status", 500),
            entry("instance", URI.create("urn:invalid-uri-syntax?" +
                "source=evil%0Anewlines&exception=java.net.URISyntaxException%3A+" +
                "Illegal+character+in+path+at+index+4%3A+evil%0Anewlines"))
        );
    }

    @Test void shouldMapCustomExceptionWithSpacesInInstanceMethod() {
        class SomeException extends RuntimeException {
            @Instance private String instance() { return "spaces are invalid"; }
        }

        Response response = mapper.toResponse(new SomeException());

        then(response.getStatusInfo()).isEqualTo(INTERNAL_SERVER_ERROR);
        then(problemDetailAsMap(response)).containsExactly(
            entry("type", URI.create("urn:problem-type:some")),
            entry("title", "Some"),
            entry("status", 500),
            entry("instance", URI.create("urn:spaces+are+invalid"))
        );
    }

    @Test void shouldMapCustomExceptionWithSpacesInInstanceField() {
        class SomeException extends RuntimeException {
            @Instance private String instance = "spaces are invalid";
        }

        Response response = mapper.toResponse(new SomeException());

        then(response.getStatusInfo()).isEqualTo(INTERNAL_SERVER_ERROR);
        then(problemDetailAsMap(response)).containsExactly(
            entry("type", URI.create("urn:problem-type:some")),
            entry("title", "Some"),
            entry("status", 500),
            entry("instance", URI.create("urn:spaces+are+invalid"))
        );
    }

    @Test void shouldMapCustomExceptionWithNullInstanceMethod() {
        class SomeException extends RuntimeException {
            @Instance private String instance() { return null; }
        }

        Response response = mapper.toResponse(new SomeException());

        then(response.getStatusInfo()).isEqualTo(INTERNAL_SERVER_ERROR);
        Map<String, Object> map = problemDetailAsMap(response);
        then(map).contains(
            entry("type", URI.create("urn:problem-type:some")),
            entry("title", "Some"),
            entry("status", 500))
            .containsKey("instance");
        then(map.get("instance").toString()).startsWith("urn:uuid:");
    }

    @Test void shouldMapCustomExceptionWithParameterizedInstanceMethod() {
        class SomeException extends RuntimeException {
            @Instance private String instance(String foo) { return "bar"; }
        }

        Response response = mapper.toResponse(new SomeException());

        then(response.getStatusInfo()).isEqualTo(INTERNAL_SERVER_ERROR);
        Map<String, Object> map = problemDetailAsMap(response);
        then(map).contains(
            entry("type", URI.create("urn:problem-type:some")),
            entry("title", "Some"),
            entry("status", 500))
            .containsKey("instance");
        URI instance = (URI) map.get("instance");
        then(instance.getScheme()).isEqualTo("urn");
        then(instance.getSchemeSpecificPart())
            .isEqualTo("could+not+invoke+SomeException.instance:+expected+no+args+but+got+1");
    }

    @Test void shouldMapCustomExceptionWithNullInstanceField() {
        class SomeException extends RuntimeException {
            @Instance private String instance = null;
        }

        Response response = mapper.toResponse(new SomeException());

        then(response.getStatusInfo()).isEqualTo(INTERNAL_SERVER_ERROR);
        Map<String, Object> map = problemDetailAsMap(response);
        then(map).contains(
            entry("type", URI.create("urn:problem-type:some")),
            entry("title", "Some"),
            entry("status", 500))
            .containsKey("instance");
        then(map.get("instance").toString()).startsWith("urn:uuid:");
    }


    @Test void shouldMapCustomExceptionWithTwoInstanceMethods() {
        class SomeException extends RuntimeException {
            @Instance private String instance() { return "foo"; }

            @Instance private String instance2() { return "foo"; }
        }

        Response response = mapper.toResponse(new SomeException());

        then(response.getStatusInfo()).isEqualTo(INTERNAL_SERVER_ERROR);
        Map<String, Object> map = problemDetailAsMap(response);
        then(map).contains(
            entry("type", URI.create("urn:problem-type:some")),
            entry("title", "Some"),
            entry("status", 500),
            entry("instance", URI.create("foo")));
    }

    @Test void shouldMapCustomExceptionWithTwoInstanceFields() {
        class SomeException extends RuntimeException {
            @Instance private String instance = "foo";
            @Instance private String instance2 = "foo";
        }

        Response response = mapper.toResponse(new SomeException());

        then(response.getStatusInfo()).isEqualTo(INTERNAL_SERVER_ERROR);
        Map<String, Object> map = problemDetailAsMap(response);
        then(map).contains(
            entry("type", URI.create("urn:problem-type:some")),
            entry("title", "Some"),
            entry("status", 500),
            entry("instance", URI.create("foo")));
    }

    @SuppressWarnings("unchecked") private Map<String, Object> problemDetailAsMap(Response problemDetail) {
        return (Map<String, Object>) problemDetail.getEntity();
    }
}
