package test;

import com.github.t1.problemdetail.ri.lib.ProblemDetail;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;

import static com.github.t1.problemdetail.Constants.PROBLEM_DETAIL_JSON;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.BDDAssertions.then;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static test.ContainerLaunchingExtension.testPost;

/** The difference between this class and the same class in the `test` module is only in the imports */
@ExtendWith(ContainerLaunchingExtension.class)
class ValidationFailedExceptionMappingIT {

    @Test void shouldMapValidationFailedException() {
        testPost("/validation", ValidationProblemDetail.class)
            .hasStatus(BAD_REQUEST)
            .hasContentType(PROBLEM_DETAIL_JSON)
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
