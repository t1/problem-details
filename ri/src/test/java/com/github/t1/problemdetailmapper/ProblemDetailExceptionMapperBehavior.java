package com.github.t1.problemdetailmapper;

import com.github.t1.problemdetail.Detail;
import com.github.t1.problemdetail.Instance;
import com.github.t1.problemdetail.ProblemExtension;
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

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class ProblemDetailExceptionMapperBehavior {

    private ProblemDetailExceptionMapper mapper = new ProblemDetailExceptionMapper();

    @BeforeEach void setUp() {
        mapper.requestHeaders = new ResteasyHttpHeaders(new MultivaluedHashMap<>());
    }

    @Test void shouldMapStandardRuntimeException() {
        Response problemDetail = mapper.toResponse(new NullPointerException("some message"));

        assertThat(problemDetail.getStatusInfo()).isEqualTo(INTERNAL_SERVER_ERROR);
        assertThat(problemDetailAsMap(problemDetail))
            .contains(
                entry("type", URI.create("urn:problem-type:null-pointer")),
                entry("title", "Null Pointer"),
                entry("status", 500),
                entry("detail", "some message"))
            .hasSize(5) // the URN is random
            .containsKey("instance");
    }

    @Test void shouldMapStandardIllegalArgumentException() {
        Response problemDetail = mapper.toResponse(new IllegalArgumentException("some message"));

        assertThat(problemDetail.getStatusInfo()).isEqualTo(BAD_REQUEST);
        assertThat(problemDetailAsMap(problemDetail))
            .contains(
                entry("type", URI.create("urn:problem-type:illegal-argument")),
                entry("title", "Illegal Argument"),
                entry("status", 400),
                entry("detail", "some message"))
            .hasSize(5) // the URN is random
            .containsKey("instance");
    }

    @Test void shouldMapWebApplicationException() {
        Response problemDetail = mapper.toResponse(new ForbiddenException("some message"));

        assertThat(problemDetail.getStatusInfo()).isEqualTo(FORBIDDEN);
        assertThat(problemDetailAsMap(problemDetail))
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

        assertThat(out.getStatusInfo()).isEqualTo(INTERNAL_SERVER_ERROR);
        assertThat(out.getEntity()).isSameAs(in.getEntity());
    }

    @Test void shouldMapCustomExceptionWithFields() {
        @Type("some-type")
        @Title("some-title")
        @Status(FORBIDDEN)
        class SomeException extends RuntimeException {
            @ProblemExtension private final int f1 = 123;
            @SuppressWarnings("unused") private final int unmapped = 456;
            @Instance private final URI instance = URI.create("https://some.domain/some/path");
            @Detail String detail = "some-detail";
        }

        Response problemDetail = mapper.toResponse(new SomeException());

        assertThat(problemDetail.getStatusInfo()).isEqualTo(FORBIDDEN);
        assertThat(problemDetailAsMap(problemDetail)).containsExactly(
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
            @ProblemExtension private int f1() { return 123; }

            @Instance private URI instance() { return URI.create("https://some.domain/some/path"); }

            @Detail String detail() { return "some-detail"; }
        }

        Response problemDetail = mapper.toResponse(new SomeException());

        assertThat(problemDetail.getStatusInfo()).isEqualTo(FORBIDDEN);
        assertThat(problemDetailAsMap(problemDetail)).containsExactly(
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
            @ProblemExtension private int f1() { throw new RuntimeException("no f1"); }

            @Instance private URI instance() { throw new IllegalArgumentException("no instance"); }

            @Detail String detail() { throw new NullPointerException(); }
        }

        Response problemDetail = mapper.toResponse(new SomeException());

        assertThat(problemDetail.getStatusInfo()).isEqualTo(FORBIDDEN);
        assertThat(problemDetailAsMap(problemDetail)).containsExactly(
            entry("type", URI.create("some-type")),
            entry("title", "some-title"),
            entry("status", 403),
            entry("detail", "could not invoke SomeException.detail: java.lang.NullPointerException"),
            entry("instance", URI.create("urn:invalid-uri-syntax?" +
                "source=could+not+invoke+SomeException.instance%3A+java.lang.IllegalArgumentException%3A+no+instance&" +
                "exception=java.net.URISyntaxException%3A+Illegal+character+in+scheme+name+at+index+5%3A+could+not+invoke+SomeException.instance%3A+java.lang.IllegalArgumentException%3A+no+instance")),
            entry("f1", "could not invoke SomeException.f1: java.lang.RuntimeException: no f1")
        );
    }

    @Test void shouldMapCustomExceptionWithInvalidInstanceMethod() {
        class SomeException extends RuntimeException {
            @Instance private String instance() { return "spaces are invalid"; }
        }

        Response problemDetail = mapper.toResponse(new SomeException());

        assertThat(problemDetail.getStatusInfo()).isEqualTo(INTERNAL_SERVER_ERROR);
        assertThat(problemDetailAsMap(problemDetail)).containsExactly(
            entry("type", URI.create("urn:problem-type:some")),
            entry("title", "Some"),
            entry("status", 500),
            entry("instance", URI.create("urn:invalid-uri-syntax?" +
                "source=spaces+are+invalid&exception=java.net.URISyntaxException%3A+" +
                "Illegal+character+in+path+at+index+6%3A+spaces+are+invalid"))
        );
    }

    @SuppressWarnings("unchecked") private Map<String, Object> problemDetailAsMap(Response problemDetail) {
        return (Map<String, Object>) problemDetail.getEntity();
    }
}
