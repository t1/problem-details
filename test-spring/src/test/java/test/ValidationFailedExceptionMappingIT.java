package test;

import com.github.t1.problemdetail.ri.lib.ProblemDetail;
import com.github.t1.problemdetaildemoapp.ValidationBoundary.Address;
import com.github.t1.problemdetaildemoapp.ValidationBoundary.Person;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Map;

import static com.github.t1.problemdetail.Constants.PROBLEM_DETAIL_JSON;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.assertj.core.api.BDDAssertions.then;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static test.ContainerLaunchingExtension.BASE_URI;
import static test.ContainerLaunchingExtension.createRestTemplate;
import static test.ContainerLaunchingExtension.then;

/** The difference between this class and the same class in the `test` module is only in the imports */
@ExtendWith(ContainerLaunchingExtension.class)
class ValidationFailedExceptionMappingIT {

    @Test void shouldMapManualValidationFailedException() {
        ResponseEntity<ValidationProblemDetail> response = createRestTemplate()
            .postForEntity(BASE_URI + "/validation/manual", null, ValidationProblemDetail.class);

        thenValidationFailed(response);
    }

    @Test void shouldMapAnnotatedValidationFailedException() {
        Person person = new Person(null, "", LocalDate.now().plusDays(3),
            new Address[]{new Address(null, -1, null)});

        ResponseEntity<ValidationProblemDetail> response = createRestTemplate()
            .postForEntity(BASE_URI + "/validation/annotated", person, ValidationProblemDetail.class);

        assumeThat(response.getStatusCode())
            .describedAs("@Valid annotation not yet caught by Spring Boot RI") // TODO map @Valid
            .isEqualTo(BAD_REQUEST);
        thenValidationFailed(response);
    }

    @Data @EqualsAndHashCode(callSuper = true)
    public static class ValidationProblemDetail extends ProblemDetail {
        private Map<String, String> violations;
    }

    private void thenValidationFailed(ResponseEntity<ValidationProblemDetail> response) {
        then(response)
            .hasStatus(BAD_REQUEST)
            .hasContentType(PROBLEM_DETAIL_JSON)
            .hasType("urn:problem-type:validation-failed")
            .hasTitle("Validation Failed")
            .hasDetail("6 violations failed")
            .hasUuidInstance()
            .checkExtensions(detail -> then(detail.violations).containsOnly(
                entry("firstName", "must not be null"),
                entry("lastName", "must not be empty"),
                entry("born", "must be a past date"),
                entry("address[0].street", "must not be null"),
                entry("address[0].zipCode", "must be greater than 0"),
                entry("address[0].city", "must not be null")
            ));
    }
}
