package test;

import com.github.t1.problemdetail.ProblemDetail;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import static com.github.t1.problemdetail.Constants.PROBLEM_DETAIL_JSON;
import static com.github.t1.problemdetail.Constants.PROBLEM_DETAIL_XML;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.BDDAssertions.then;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static test.TestTools.createRestTemplate;
import static test.TestTools.post;
import static test.TestTools.then;

class StandardExceptionMappingIT {

    @Test void shouldMapClientWebApplicationExceptionWithoutEntityOrMessage() {
        ResponseEntity<ProblemDetail> response = post("standard/plain-bad-request");

        then(response)
            .hasStatus(BAD_REQUEST)
            .hasMediaType(PROBLEM_DETAIL_JSON)
            .hasType("urn:problem-type:bad-request")
            .hasTitle("Bad Request")
            .hasDetail(null)
            .hasUuidInstance();
    }

    @Test void shouldMapClientWebApplicationExceptionWithoutEntityButMessage() {
        ResponseEntity<ProblemDetail> response = post("/standard/bad-request-with-message");

        then(response)
            .hasStatus(BAD_REQUEST)
            .hasMediaType(PROBLEM_DETAIL_JSON)
            .hasType("urn:problem-type:bad-request")
            .hasTitle("Bad Request")
            .hasDetail("some message")
            .hasUuidInstance();
    }

    @Test void shouldUseEntityFromWebApplicationException() {
        RestTemplate restTemplate = createRestTemplate();
        restTemplate.setMessageConverters(singletonList(new StringHttpMessageConverter()));
        ResponseEntity<String> response = post(restTemplate, "/standard/bad-request-with-text-response", String.class);

        then(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        then(response.getHeaders().getContentType()).hasToString("text/plain");
        then(response.getBody()).isEqualTo("the body");
    }

    @Test void shouldMapServerWebApplicationExceptionWithoutEntityOrMessage() {
        ResponseEntity<ProblemDetail> response = post("/standard/plain-service-unavailable");

        then(response)
            .hasStatus(SERVICE_UNAVAILABLE)
            .hasMediaType(PROBLEM_DETAIL_JSON)
            .hasType("urn:problem-type:service-unavailable")
            .hasTitle("Service Unavailable")
            .hasDetail(null)
            .hasUuidInstance();
    }

    @Test void shouldMapIllegalArgumentExceptionWithoutMessage() {
        ResponseEntity<ProblemDetail> response = post("/standard/illegal-argument-without-message");

        then(response)
            .hasStatus(BAD_REQUEST)
            .hasMediaType(PROBLEM_DETAIL_JSON)
            .hasType("urn:problem-type:illegal-argument")
            .hasTitle("Illegal Argument")
            .hasDetail(null)
            .hasUuidInstance();
    }

    @Test void shouldMapIllegalArgumentExceptionWithMessage() {
        ResponseEntity<ProblemDetail> response = post("/standard/illegal-argument-with-message");

        then(response)
            .hasStatus(BAD_REQUEST)
            .hasMediaType(PROBLEM_DETAIL_JSON)
            .hasType("urn:problem-type:illegal-argument")
            .hasTitle("Illegal Argument")
            .hasDetail("some message")
            .hasUuidInstance();
    }

    @Test void shouldMapNullPointerExceptionWithoutMessage() {
        ResponseEntity<ProblemDetail> response = post("/standard/npe-without-message");

        then(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasMediaType(PROBLEM_DETAIL_JSON)
            .hasType("urn:problem-type:null-pointer")
            .hasTitle("Null Pointer")
            .hasDetail(null)
            .hasUuidInstance();
    }

    @Test void shouldMapNullPointerExceptionWithMessage() {
        ResponseEntity<ProblemDetail> response = post("/standard/npe-with-message");

        then(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasMediaType(PROBLEM_DETAIL_JSON)
            .hasType("urn:problem-type:null-pointer")
            .hasTitle("Null Pointer")
            .hasDetail("some message")
            .hasUuidInstance();
    }

    @Disabled("problems with jaxb on jdk11")
    @Test void shouldMapToXml() {
        RestTemplate restTemplate = createRestTemplate();
        restTemplate.setMessageConverters(singletonList(new Jaxb2RootElementHttpMessageConverter()));
        ResponseEntity<ProblemDetail> response = post(restTemplate, "/standard/npe-with-message");

        then(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasMediaType(PROBLEM_DETAIL_XML)
            .hasType("urn:problem-type:null-pointer")
            .hasTitle("Null Pointer")
            .hasDetail("some message")
            .hasUuidInstance();
    }

    @Disabled("problems with jaxb on jdk11")
    @Test void shouldMapToSecondAcceptXml() {
        RestTemplate restTemplate = createRestTemplate();
        restTemplate.setMessageConverters(asList(
            new StringHttpMessageConverter(), new Jaxb2RootElementHttpMessageConverter()));
        ResponseEntity<ProblemDetail> response = post(restTemplate, "/standard/npe-with-message");

        then(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasMediaType(PROBLEM_DETAIL_XML)
            .hasType("urn:problem-type:null-pointer")
            .hasTitle("Null Pointer")
            .hasDetail("some message")
            .hasUuidInstance();
    }
}
