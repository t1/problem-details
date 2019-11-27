package test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.ws.rs.core.Response;

import static com.github.t1.problemdetail.Constants.PROBLEM_DETAIL_JSON_TYPE;
import static com.github.t1.problemdetail.Constants.PROBLEM_DETAIL_XML_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_XML_TYPE;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
import static org.assertj.core.api.BDDAssertions.then;
import static test.ProblemDetailMapperExtension.then;

class StandardExceptionMappingIT {
    @RegisterExtension static ProblemDetailMapperExtension mapper = new ProblemDetailMapperExtension();

    @Test void shouldMapClientWebApplicationExceptionWithoutEntityOrMessage() {
        Response response = mapper.post("/standard/plain-bad-request");

        then(response)
            .hasStatus(BAD_REQUEST)
            .hasMediaType(PROBLEM_DETAIL_JSON_TYPE)
            .hasType("urn:problem-type:bad-request")
            .hasTitle("Bad Request")
            .hasDetail(null)
            .hasUuidInstance();
    }

    @Test void shouldMapClientWebApplicationExceptionWithoutEntityButMessage() {
        Response response = mapper.post("/standard/bad-request-with-message");

        then(response)
            .hasStatus(BAD_REQUEST)
            .hasMediaType(PROBLEM_DETAIL_JSON_TYPE)
            .hasType("urn:problem-type:bad-request")
            .hasTitle("Bad Request")
            .hasDetail("some message")
            .hasUuidInstance();
    }

    @Test void shouldUseEntityFromWebApplicationException() {
        Response response = mapper.post("/standard/bad-request-with-response");

        then(response.getStatusInfo()).isEqualTo(BAD_REQUEST);
        then(response.getMediaType()).isIn(TEXT_PLAIN_TYPE, TEXT_PLAIN_TYPE.withCharset("UTF-8"));
        then(response.readEntity(String.class)).isEqualTo("the body");
    }

    @Test void shouldMapServerWebApplicationExceptionWithoutEntityOrMessage() {
        Response response = mapper.post("/standard/plain-service-unavailable");

        then(response)
            .hasStatus(SERVICE_UNAVAILABLE)
            .hasMediaType(PROBLEM_DETAIL_JSON_TYPE)
            .hasType("urn:problem-type:service-unavailable")
            .hasTitle("Service Unavailable")
            .hasDetail(null)
            .hasUuidInstance();
    }

    @Test void shouldMapIllegalArgumentExceptionWithoutMessage() {
        Response response = mapper.post("/standard/illegal-argument-without-message");

        then(response)
            .hasStatus(BAD_REQUEST)
            .hasMediaType(PROBLEM_DETAIL_JSON_TYPE)
            .hasType("urn:problem-type:illegal-argument")
            .hasTitle("Illegal Argument")
            .hasDetail(null)
            .hasUuidInstance();
    }

    @Test void shouldMapIllegalArgumentExceptionWithMessage() {
        Response response = mapper.post("/standard/illegal-argument-with-message");

        then(response)
            .hasStatus(BAD_REQUEST)
            .hasMediaType(PROBLEM_DETAIL_JSON_TYPE)
            .hasType("urn:problem-type:illegal-argument")
            .hasTitle("Illegal Argument")
            .hasDetail("some message")
            .hasUuidInstance();
    }

    @Test void shouldMapNullPointerExceptionWithoutMessage() {
        Response response = mapper.post("/standard/npe-without-message");

        then(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasMediaType(PROBLEM_DETAIL_JSON_TYPE)
            .hasType("urn:problem-type:null-pointer")
            .hasTitle("Null Pointer")
            .hasDetail(null)
            .hasUuidInstance();
    }

    @Test void shouldMapNullPointerExceptionWithMessage() {
        Response response = mapper.post("/standard/npe-with-message");

        then(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasMediaType(PROBLEM_DETAIL_JSON_TYPE)
            .hasType("urn:problem-type:null-pointer")
            .hasTitle("Null Pointer")
            .hasDetail("some message")
            .hasUuidInstance();
    }

    @Test void shouldMapToXml() {
        Response response = mapper.target("/standard/npe-with-message")
            .request(APPLICATION_XML_TYPE).post(null);

        then(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasMediaType(PROBLEM_DETAIL_XML_TYPE)
            .hasType("urn:problem-type:null-pointer")
            .hasTitle("Null Pointer")
            .hasDetail("some message")
            .hasUuidInstance();
    }

    @Test void shouldMapToSecondAcceptXml() {
        Response response = mapper.target("/standard/npe-with-message")
            .request(TEXT_PLAIN_TYPE, APPLICATION_XML_TYPE).post(null);

        then(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasMediaType(PROBLEM_DETAIL_XML_TYPE)
            .hasType("urn:problem-type:null-pointer")
            .hasTitle("Null Pointer")
            .hasDetail("some message")
            .hasUuidInstance();
    }
}
