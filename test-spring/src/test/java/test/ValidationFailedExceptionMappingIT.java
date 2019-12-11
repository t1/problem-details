package test;

import com.github.t1.problemdetail.ProblemDetail;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static com.github.t1.problemdetail.Constants.PROBLEM_DETAIL_JSON;
import static org.assertj.core.api.Assertions.entry;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static test.TestTools.post;
import static test.TestTools.then;

class ValidationFailedExceptionMappingIT {
    @Test void shouldMapValidationFailedException() {
        ResponseEntity<ValidationProblemDetail> response = post("/validation", ValidationProblemDetail.class);

        then(response)
            .hasStatus(BAD_REQUEST)
            .hasMediaType(PROBLEM_DETAIL_JSON)
            .hasType("urn:problem-type:validation-failed")
            .hasTitle("Validation Failed")
            .hasDetail("6 violations failed")
            .hasUuidInstance()
            .checkExtensions(detail -> BDDAssertions.then(detail.violations).containsOnly(
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
