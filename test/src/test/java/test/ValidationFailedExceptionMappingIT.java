package test;

import com.github.t1.problemdetail.ProblemDetail;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;
import java.util.Set;

import static com.github.t1.problemdetail.Constants.PROBLEM_DETAIL_JSON;
import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.BDDAssertions.then;
import static test.ContainerLaunchingExtension.testPost;

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
                entry("lastName", Set.of("must not be null")),
                entry("address[0].city", Set.of("must not be null")),
                entry("address[0].street", Set.of("must not be null")),
                entry("address[0].zipCode", Set.of("must be greater than 0")),
                entry("firstName", Set.of("must not be null")),
                entry("born", Set.of("must be a past date"))
            ));
    }

    @Data @EqualsAndHashCode(callSuper = true)
    public static class ValidationProblemDetail extends ProblemDetail {
        private Map<String, Set<String>> violations;
    }
}
