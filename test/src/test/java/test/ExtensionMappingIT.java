package test;

import com.github.t1.problemdetail.ProblemDetail;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.ws.rs.core.Response;

import static com.github.t1.problemdetail.Constants.PROBLEM_DETAIL_JSON_TYPE;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static org.assertj.core.api.BDDAssertions.then;
import static test.ProblemDetailMapperExtension.then;

class ExtensionMappingIT {
    @RegisterExtension static ProblemDetailMapperExtension mapper = new ProblemDetailMapperExtension();

    @Test void shouldMapExtensionStringMethod() {
        Response response = mapper.post("/custom/extension-method");

        then(response, ProblemDetailWithExtensionString.class)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasMediaType(PROBLEM_DETAIL_JSON_TYPE)
            .hasType("urn:problem-type:some")
            .hasTitle("Some")
            .hasDetail(null)
            .hasUuidInstance()
            .checkExtensions(detail -> then(detail.ex).isEqualTo("some extension"));
    }

    @Test void shouldMapExtensionStringMethodWithAnnotatedName() {
        Response response = mapper.post("/custom/extension-method-with-name");

        then(response, ProblemDetailWithExtensionStringFoo.class)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasMediaType(PROBLEM_DETAIL_JSON_TYPE)
            .hasType("urn:problem-type:some-message")
            .hasTitle("Some Message")
            .hasDetail(null)
            .hasUuidInstance()
            .checkExtensions(detail -> then(detail.foo).isEqualTo("some extension"));
    }

    @Test void shouldMapExtensionStringField() {
        Response response = mapper.post("/custom/extension-field");

        then(response, ProblemDetailWithExtensionString.class)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasMediaType(PROBLEM_DETAIL_JSON_TYPE)
            .hasType("urn:problem-type:some-message")
            .hasTitle("Some Message")
            .hasDetail(null)
            .hasUuidInstance()
            .checkExtensions(detail -> then(detail.ex).isEqualTo("some extension"));
    }

    @Test void shouldMapExtensionStringFieldWithAnnotatedName() {
        Response response = mapper.post("/custom/extension-field-with-name");

        then(response, ProblemDetailWithExtensionStringFoo.class)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasMediaType(PROBLEM_DETAIL_JSON_TYPE)
            .hasType("urn:problem-type:some-message")
            .hasTitle("Some Message")
            .hasDetail(null)
            .hasUuidInstance()
            .checkExtensions(detail -> then(detail.foo).isEqualTo("some extension"));
    }

    @Test void shouldMapMultiplePackagePrivateExtensions() {
        Response response = mapper.post("/custom/multi-extension");

        then(response, ProblemDetailWithMultipleExtensions.class)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasMediaType(PROBLEM_DETAIL_JSON_TYPE)
            .hasType("urn:problem-type:some-message")
            .hasTitle("Some Message")
            .hasDetail(null)
            .hasUuidInstance()
            .checkExtensions(detail -> {
                then(detail.m1).isEqualTo("method 1");
                then(detail.m2).isEqualTo("method 2");
                then(detail.f1).isEqualTo("field 1");
                then(detail.f2).isEqualTo("field 2");
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
