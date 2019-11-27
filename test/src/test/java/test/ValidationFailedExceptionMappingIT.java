package test;

import com.github.t1.problemdetail.ProblemDetail;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.ws.rs.core.Response;
import java.util.Map;

import static com.github.t1.problemdetail.Constants.PROBLEM_DETAIL_JSON_TYPE;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.BDDAssertions.then;
import static test.ProblemDetailMapperExtension.then;

class ValidationFailedExceptionMappingIT {
    @RegisterExtension static ProblemDetailMapperExtension mapper = new ProblemDetailMapperExtension();

    @Test void shouldMapValidationFailedException() {
        Response response = mapper.post("/validation");

        then(response, ValidationProblemDetail.class)
            .hasStatus(BAD_REQUEST)
            .hasMediaType(PROBLEM_DETAIL_JSON_TYPE)
            .hasType("urn:problem-type:validation-failed")
            .hasTitle("Validation Failed")
            .hasDetail("6 violations failed")
            .hasUuidInstance()
            .checkExtensions(detail -> then(detail.violations).containsOnly(
                entry("lastName", "must not be null"),
                entry("address[0].city", "must not be null"),
                entry("address[0].street", "must not be null"),
                entry("address[0].zipCode", "must be greater than 0"),
                entry("firstName", "must not be null"),
                entry("born", "must be a past date")
            ));
    }

    @Data @EqualsAndHashCode(callSuper = true)
    public static class ValidationProblemDetail extends ProblemDetail {
        private Map<String, String> violations;
    }
}
