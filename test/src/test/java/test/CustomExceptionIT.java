package test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.ws.rs.core.Response;

import static com.github.t1.problemdetail.Constants.PROBLEM_DETAIL_JSON_TYPE;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static test.ProblemDetailMapperExtension.then;

class CustomExceptionIT {
    @RegisterExtension static ProblemDetailMapperExtension mapper = new ProblemDetailMapperExtension();

    @Test void shouldMapCustomRuntimeException() {
        Response response = mapper.post("/custom/runtime-exception");

        then(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasMediaType(PROBLEM_DETAIL_JSON_TYPE)
            .hasType("urn:problem-type:custom")
            .hasTitle("Custom")
            .hasDetail(null)
            .hasUuidInstance();
    }

    @Test void shouldMapCustomIllegalArgumentException() {
        Response response = mapper.post("/custom/illegal-argument-exception");

        then(response)
            .hasStatus(BAD_REQUEST)
            .hasMediaType(PROBLEM_DETAIL_JSON_TYPE)
            .hasType("urn:problem-type:custom")
            .hasTitle("Custom")
            .hasDetail(null)
            .hasUuidInstance();
    }

    @Test void shouldMapExplicitType() {
        Response response = mapper.post("/custom/explicit-type");

        then(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasMediaType(PROBLEM_DETAIL_JSON_TYPE)
            .hasType("http://error-codes.org/out-of-memory")
            .hasTitle("Some")
            .hasDetail(null)
            .hasUuidInstance();
    }

    @Test void shouldMapExplicitTitle() {
        Response response = mapper.post("/custom/explicit-title");

        then(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasMediaType(PROBLEM_DETAIL_JSON_TYPE)
            .hasType("urn:problem-type:some")
            .hasTitle("Some Title")
            .hasDetail(null)
            .hasUuidInstance();
    }

    @Test void shouldMapExplicitStatus() {
        Response response = mapper.post("/custom/explicit-status");

        then(response)
            .hasStatus(FORBIDDEN)
            .hasMediaType(PROBLEM_DETAIL_JSON_TYPE)
            .hasType("urn:problem-type:something-forbidden")
            .hasTitle("Something Forbidden")
            .hasDetail(null)
            .hasUuidInstance();
    }


    @Test void shouldMapDetailMethod() {
        Response response = mapper.post("/custom/public-detail-method");

        then(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasMediaType(PROBLEM_DETAIL_JSON_TYPE)
            .hasType("urn:problem-type:some-message")
            .hasTitle("Some Message")
            .hasDetail("some detail")
            .hasUuidInstance();
    }

    @Test void shouldMapPrivateDetailMethod() {
        Response response = mapper.post("/custom/private-detail-method");

        then(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasMediaType(PROBLEM_DETAIL_JSON_TYPE)
            .hasType("urn:problem-type:some-message")
            .hasTitle("Some Message")
            .hasDetail("some detail")
            .hasUuidInstance();
    }

    @Test void shouldMapFailingDetailMethod() {
        Response response = mapper.post("/custom/failing-detail-method");

        then(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasMediaType(PROBLEM_DETAIL_JSON_TYPE)
            .hasType("urn:problem-type:failing-detail")
            .hasTitle("Failing Detail")
            .hasDetail("could not invoke FailingDetailException.failingDetail: java.lang.RuntimeException: inner")
            .hasUuidInstance();
    }

    @Test void shouldMapPublicDetailFieldOverridingMessage() {
        Response response = mapper.post("/custom/public-detail-field");

        then(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasMediaType(PROBLEM_DETAIL_JSON_TYPE)
            .hasType("urn:problem-type:some-message")
            .hasTitle("Some Message")
            .hasDetail("some detail")
            .hasUuidInstance();
    }

    @Test void shouldMapPrivateDetailField() {
        Response response = mapper.post("/custom/private-detail-field");

        then(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasMediaType(PROBLEM_DETAIL_JSON_TYPE)
            .hasType("urn:problem-type:some-message")
            .hasTitle("Some Message")
            .hasDetail("some detail")
            .hasUuidInstance();
    }

    @Test void shouldMapMultipleDetailFields() {
        Response response = mapper.post("/custom/multi-detail-fields");

        then(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasMediaType(PROBLEM_DETAIL_JSON_TYPE)
            .hasType("urn:problem-type:some-message")
            .hasTitle("Some Message")
            .hasDetail("detail a. detail b")
            .hasUuidInstance();
    }

    @Test void shouldMapDetailMethodAndTwoFields() {
        Response response = mapper.post("/custom/mixed-details");

        then(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasMediaType(PROBLEM_DETAIL_JSON_TYPE)
            .hasType("urn:problem-type:some-message")
            .hasTitle("Some Message")
            .hasDetail("detail a. detail b. detail c")
            .hasUuidInstance();
    }

    @Test void shouldFailToMapDetailMethodTakingAnArgument() {
        Response response = mapper.post("/custom/detail-method-arg");

        then(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasMediaType(PROBLEM_DETAIL_JSON_TYPE)
            .hasType("urn:problem-type:some-message")
            .hasTitle("Some Message")
            .hasDetail("could not invoke SomeMessageException.detail: expected no args but got 1")
            .hasUuidInstance();
    }
}
