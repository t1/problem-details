package test;

import com.github.t1.problemdetail.ProblemDetail;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static com.github.t1.problemdetail.Constants.PROBLEM_DETAIL_JSON;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static test.TestTools.post;
import static test.TestTools.then;

class ExtensionMappingIT {
    @Test void shouldMapExtensionStringMethod() {
        ResponseEntity<ProblemDetailWithExtensionString> response =
            post("/custom/extension-method", ProblemDetailWithExtensionString.class);

        then(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasMediaType(PROBLEM_DETAIL_JSON)
            .hasType("urn:problem-type:some")
            .hasTitle("Some")
            .hasDetail(null)
            .hasUuidInstance()
            .checkExtensions(detail -> BDDAssertions.then(detail.ex).isEqualTo("some extension"));
    }

    @Test void shouldMapExtensionStringMethodWithAnnotatedName() {
        ResponseEntity<ProblemDetailWithExtensionStringFoo> response =
            post("/custom/extension-method-with-name", ProblemDetailWithExtensionStringFoo.class);

        then(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasMediaType(PROBLEM_DETAIL_JSON)
            .hasType("urn:problem-type:some-message")
            .hasTitle("Some Message")
            .hasDetail(null)
            .hasUuidInstance()
            .checkExtensions(detail -> BDDAssertions.then(detail.foo).isEqualTo("some extension"));
    }

    @Test void shouldMapExtensionStringField() {
        ResponseEntity<ProblemDetailWithExtensionString> response =
            post("/custom/extension-field", ProblemDetailWithExtensionString.class);

        then(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasMediaType(PROBLEM_DETAIL_JSON)
            .hasType("urn:problem-type:some-message")
            .hasTitle("Some Message")
            .hasDetail(null)
            .hasUuidInstance()
            .checkExtensions(detail -> BDDAssertions.then(detail.ex).isEqualTo("some extension"));
    }

    @Test void shouldMapExtensionStringFieldWithAnnotatedName() {
        ResponseEntity<ProblemDetailWithExtensionStringFoo> response =
            post("/custom/extension-field-with-name", ProblemDetailWithExtensionStringFoo.class);

        then(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasMediaType(PROBLEM_DETAIL_JSON)
            .hasType("urn:problem-type:some-message")
            .hasTitle("Some Message")
            .hasDetail(null)
            .hasUuidInstance()
            .checkExtensions(detail -> BDDAssertions.then(detail.foo).isEqualTo("some extension"));
    }

    @Test void shouldMapMultiplePackagePrivateExtensions() {
        ResponseEntity<ProblemDetailWithMultipleExtensions> response =
            post("/custom/multi-extension", ProblemDetailWithMultipleExtensions.class);

        then(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasMediaType(PROBLEM_DETAIL_JSON)
            .hasType("urn:problem-type:some-message")
            .hasTitle("Some Message")
            .hasDetail(null)
            .hasUuidInstance()
            .checkExtensions(detail -> {
                BDDAssertions.then(detail.m1).isEqualTo("method 1");
                BDDAssertions.then(detail.m2).isEqualTo("method 2");
                BDDAssertions.then(detail.f1).isEqualTo("field 1");
                BDDAssertions.then(detail.f2).isEqualTo("field 2");
            });
    }

    @Data @EqualsAndHashCode(callSuper = true)
    public static class ProblemDetailWithExtensionString extends ProblemDetail {
        private String ex;
    }

    @Data @EqualsAndHashCode(callSuper = true)
    public static class ProblemDetailWithExtensionStringFoo extends ProblemDetail {
        private String foo;
    }

    @Data @EqualsAndHashCode(callSuper = true)
    public static class ProblemDetailWithMultipleExtensions extends ProblemDetail {
        private String m1;
        private String m2;
        private String f1;
        private String f2;
    }
}
