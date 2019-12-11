package test;

import com.github.t1.problemdetail.ProblemDetail;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static com.github.t1.problemdetail.Constants.PROBLEM_DETAIL_JSON;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static test.TestTools.post;
import static test.TestTools.then;

class CustomExceptionIT {
    @Test void shouldMapCustomRuntimeException() {
        ResponseEntity<ProblemDetail> response = post("/custom/runtime-exception");

        then(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasMediaType(PROBLEM_DETAIL_JSON)
            .hasType("urn:problem-type:custom")
            .hasTitle("Custom")
            .hasDetail(null)
            .hasUuidInstance();
    }

    @Test void shouldMapCustomIllegalArgumentException() {
        ResponseEntity<ProblemDetail> response = post("/custom/illegal-argument-exception");

        then(response)
            .hasStatus(BAD_REQUEST)
            .hasMediaType(PROBLEM_DETAIL_JSON)
            .hasType("urn:problem-type:custom")
            .hasTitle("Custom")
            .hasDetail(null)
            .hasUuidInstance();
    }

    @Test void shouldMapExplicitType() {
        ResponseEntity<ProblemDetail> response = post("/custom/explicit-type");

        then(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasMediaType(PROBLEM_DETAIL_JSON)
            .hasType("http://error-codes.org/out-of-memory")
            .hasTitle("Some")
            .hasDetail(null)
            .hasUuidInstance();
    }

    @Test void shouldMapExplicitTitle() {
        ResponseEntity<ProblemDetail> response = post("/custom/explicit-title");

        then(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasMediaType(PROBLEM_DETAIL_JSON)
            .hasType("urn:problem-type:some")
            .hasTitle("Some Title")
            .hasDetail(null)
            .hasUuidInstance();
    }

    @Test void shouldMapExplicitStatus() {
        ResponseEntity<ProblemDetail> response = post("/custom/explicit-status");

        then(response)
            .hasStatus(FORBIDDEN)
            .hasMediaType(PROBLEM_DETAIL_JSON)
            .hasType("urn:problem-type:something-forbidden")
            .hasTitle("Something Forbidden")
            .hasDetail(null)
            .hasUuidInstance();
    }


    @Test void shouldMapDetailMethod() {
        ResponseEntity<ProblemDetail> response = post("/custom/public-detail-method");

        then(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasMediaType(PROBLEM_DETAIL_JSON)
            .hasType("urn:problem-type:some-message")
            .hasTitle("Some Message")
            .hasDetail("some detail")
            .hasUuidInstance();
    }

    @Test void shouldMapPrivateDetailMethod() {
        ResponseEntity<ProblemDetail> response = post("/custom/private-detail-method");

        then(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasMediaType(PROBLEM_DETAIL_JSON)
            .hasType("urn:problem-type:some-message")
            .hasTitle("Some Message")
            .hasDetail("some detail")
            .hasUuidInstance();
    }

    @Test void shouldMapFailingDetailMethod() {
        ResponseEntity<ProblemDetail> response = post("/custom/failing-detail-method");

        then(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasMediaType(PROBLEM_DETAIL_JSON)
            .hasType("urn:problem-type:failing-detail")
            .hasTitle("Failing Detail")
            .hasDetail("could not invoke FailingDetailException.failingDetail: java.lang.RuntimeException: inner")
            .hasUuidInstance();
    }

    @Test void shouldMapPublicDetailFieldOverridingMessage() {
        ResponseEntity<ProblemDetail> response = post("/custom/public-detail-field");

        then(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasMediaType(PROBLEM_DETAIL_JSON)
            .hasType("urn:problem-type:some-message")
            .hasTitle("Some Message")
            .hasDetail("some detail")
            .hasUuidInstance();
    }

    @Test void shouldMapPrivateDetailField() {
        ResponseEntity<ProblemDetail> response = post("/custom/private-detail-field");

        then(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasMediaType(PROBLEM_DETAIL_JSON)
            .hasType("urn:problem-type:some-message")
            .hasTitle("Some Message")
            .hasDetail("some detail")
            .hasUuidInstance();
    }

    @Test void shouldMapMultipleDetailFields() {
        ResponseEntity<ProblemDetail> response = post("/custom/multi-detail-fields");

        then(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasMediaType(PROBLEM_DETAIL_JSON)
            .hasType("urn:problem-type:some-message")
            .hasTitle("Some Message")
            .hasDetail("detail a. detail b")
            .hasUuidInstance();
    }

    @Test void shouldMapDetailMethodAndTwoFields() {
        ResponseEntity<ProblemDetail> response = post("/custom/mixed-details");

        then(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasMediaType(PROBLEM_DETAIL_JSON)
            .hasType("urn:problem-type:some-message")
            .hasTitle("Some Message")
            .hasDetail("detail a. detail b. detail c")
            .hasUuidInstance();
    }

    @Test void shouldFailToMapDetailMethodTakingAnArgument() {
        ResponseEntity<ProblemDetail> response = post("/custom/detail-method-arg");

        then(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasMediaType(PROBLEM_DETAIL_JSON)
            .hasType("urn:problem-type:some-message")
            .hasTitle("Some Message")
            .hasDetail("could not invoke SomeMessageException.detail: expected no args but got 1")
            .hasUuidInstance();
    }
}
