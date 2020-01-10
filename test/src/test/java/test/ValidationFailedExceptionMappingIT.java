package test;

import com.github.t1.problemdetaildemoapp.ProblemDetail;
import com.github.t1.problemdetaildemoapp.ValidationBoundary.Address;
import com.github.t1.problemdetaildemoapp.ValidationBoundary.Person;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import test.ContainerLaunchingExtension.ProblemDetailAssert;

import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.t1.problemdetail.Constants.PROBLEM_DETAIL_JSON;
import static java.util.Collections.singletonList;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.BDDAssertions.then;
import static test.ContainerLaunchingExtension.target;
import static test.ContainerLaunchingExtension.then;

@ExtendWith(ContainerLaunchingExtension.class)
class ValidationFailedExceptionMappingIT {
    @Test void shouldMapManualValidationFailedException() {
        Response response = target("/validation/manual").request(APPLICATION_JSON_TYPE)
            .post(null);

        thenValidationFailed(response);
    }

    @Test void shouldMapAnnotatedValidationFailedException() {
        Person person = new Person(null, "", LocalDate.now().plusDays(3),
            singletonList(new Address(null, -1, null)));

        Response response = target("/validation/annotated").request(APPLICATION_JSON_TYPE)
            .post(entity(person, APPLICATION_JSON_TYPE));

        thenValidationFailed(response);
    }

    private void thenValidationFailed(Response response) {
        ProblemDetailAssert<ValidationProblemDetail> it = then(response, ValidationProblemDetail.class)
            .hasStatus(BAD_REQUEST)
            .hasContentType(PROBLEM_DETAIL_JSON)
            .hasType("urn:problem-type:validation-failed")
            .hasTitle("Validation Failed")
            .hasUuidInstance();

        String mustNotBeNull = mustOrMay(it) + " not be null";

        switch (it.getDetail()) {
            case "2 violations failed": // rest-easy has a different mode to validate fields
                it.check(detail -> then(detail.violations).containsOnly(
                    entry("postAnnotated.arg0.firstName", mustNotBeNull),
                    entry("postAnnotated.arg0.lastName", "must not be empty")
                ));
                break;
            case "6 violations failed":
                it.check(detail -> then(detail.violations).containsOnly(
                    entry("firstName", mustNotBeNull),
                    entry("lastName", "must not be empty"),
                    entry("born", "must be a past date"),
                    entry("address[0].street", mustNotBeNull),
                    entry("address[0].zipCode", "must be greater than 0"),
                    entry("address[0].city", mustNotBeNull)
                ));
                break;
            default:
                fail("unexpected detail: [" + it.getDetail() + "]");
        }
    }

    /** some validators say 'may' others say 'must' :( */
    private String mustOrMay(ProblemDetailAssert<ValidationProblemDetail> it) {
        AtomicBoolean containsMust = new AtomicBoolean(true);
        it.check(detail -> {
            if (detail.violations != null) {
                containsMust.set(detail.violations.containsValue("must not be null"));
            }
        });
        return containsMust.get() ? "must" : "may";
    }

    @Data @EqualsAndHashCode(callSuper = true)
    public static class ValidationProblemDetail extends ProblemDetail {
        private Map<String, String> violations;
    }
}
