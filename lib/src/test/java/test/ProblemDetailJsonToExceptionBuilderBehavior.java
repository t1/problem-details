package test;

import com.github.t1.problemdetails.jaxrs.lib.ProblemDetailExceptionRegistry;
import com.github.t1.problemdetails.jaxrs.lib.ProblemDetailJsonToExceptionBuilder;
import org.eclipse.microprofile.problemdetails.Extension;
import org.eclipse.microprofile.problemdetails.Instance;
import org.junit.jupiter.api.Test;

import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.BDDAssertions.then;

class ProblemDetailJsonToExceptionBuilderBehavior {
    private JsonObjectBuilder entity = Json.createObjectBuilder();

    @Test void shouldTriggerNullType() {
        // entity = "{}"

        IllegalArgumentException thrown = catchThrowableOfType(this::trigger, IllegalArgumentException.class);

        then(thrown).hasMessage("no registered exception found for `type` field in {}");
    }

    @Test void shouldNotBuildUnknownType() {
        entity.add("type", "unknown");

        IllegalArgumentException thrown = catchThrowableOfType(this::trigger, IllegalArgumentException.class);

        then(thrown).hasMessage("no registered exception found for `type` field in {\"type\":\"unknown\"}");
    }

    @Test void shouldBuildNullPointer() {
        entity.add("type", "urn:problem-type:null-pointer");

        NullPointerException thrown = catchThrowableOfType(this::trigger, NullPointerException.class);

        assertThat(thrown).hasMessage(null);
    }

    @Test void shouldBuildNullPointerWithMessage() {
        entity.add("type", "urn:problem-type:null-pointer");
        entity.add("detail", "some-detail");

        NullPointerException thrown = catchThrowableOfType(this::trigger, NullPointerException.class);

        assertThat(thrown).hasMessage("some-detail");
    }

    @Test void shouldBuildNullPointerWithNullDetail() {
        entity.add("type", "urn:problem-type:null-pointer");
        entity.addNull("detail");

        NullPointerException thrown = catchThrowableOfType(this::trigger, NullPointerException.class);

        assertThat(thrown).hasMessage(null);
    }

    @Test void shouldFailToBuildBadRequest() {
        entity.add("type", "urn:problem-type:bad-request");
        entity.add("detail", "some-detail");

        IllegalArgumentException thrown = catchThrowableOfType(this::trigger, IllegalArgumentException.class);

        assertThat(thrown).hasMessage("no registered exception found for `type` field in {\"type\":\"urn:problem-type:bad-request\",\"detail\":\"some-detail\"}");
    }

    public static class CustomException extends RuntimeException {}

    @Test void shouldBuildCustomType() {
        givenRegisteredType(CustomException.class);

        CustomException thrown = catchThrowableOfType(this::trigger, CustomException.class);

        assertThat(thrown).hasMessage(null);
    }

    public static class CustomWithStringInstanceException extends RuntimeException {
        @Instance String string = null;
    }

    @Test void shouldBuildCustomTypeWithStringInstanceFieldNotSet() {
        givenRegisteredType(CustomWithStringInstanceException.class);

        CustomWithStringInstanceException thrown = catchThrowableOfType(this::trigger, CustomWithStringInstanceException.class);

        assertThat(thrown).hasMessage(null);
        assertThat(thrown.string).isEqualTo(null);
    }

    @Test void shouldBuildCustomTypeWithStringInstanceFieldSet() {
        givenRegisteredType(CustomWithStringInstanceException.class);
        entity.add("instance", SOME_URI.toString());

        CustomWithStringInstanceException thrown = catchThrowableOfType(this::trigger, CustomWithStringInstanceException.class);

        assertThat(thrown).hasMessage(null);
        assertThat(thrown.string).isEqualTo(SOME_URI.toString());
    }

    public static class CustomWithUriInstanceException extends RuntimeException {
        @Instance URI uri = null;
    }

    @Test void shouldBuildCustomTypeWithUriInstanceFieldSet() {
        givenRegisteredType(CustomWithUriInstanceException.class);
        entity.add("instance", SOME_URI.toString());

        CustomWithUriInstanceException thrown = catchThrowableOfType(this::trigger, CustomWithUriInstanceException.class);

        assertThat(thrown).hasMessage(null);
        assertThat(thrown.uri).isEqualTo(SOME_URI);
    }

    public static class CustomWithIntegerInstanceException extends RuntimeException {
        @Instance Integer i = null;
    }

    @Test void shouldBuildCustomTypeWithIntegerInstanceFieldSet() {
        givenRegisteredType(CustomWithIntegerInstanceException.class);
        entity.add("instance", "123");

        CustomWithIntegerInstanceException thrown = catchThrowableOfType(this::trigger, CustomWithIntegerInstanceException.class);

        assertThat(thrown).hasMessage(null);
        assertThat(thrown.i).isEqualTo(123);
    }

    public static class CustomWithLongInstanceException extends RuntimeException {
        @Instance long l;
    }

    @Test void shouldBuildCustomTypeWithLongInstanceFieldSet() {
        givenRegisteredType(CustomWithLongInstanceException.class);
        entity.add("instance", "123");

        CustomWithLongInstanceException thrown = catchThrowableOfType(this::trigger, CustomWithLongInstanceException.class);

        assertThat(thrown).hasMessage(null);
        assertThat(thrown.l).isEqualTo(123L);
    }

    public static class CustomWithUnnamedExtensionsException extends RuntimeException {
        @Extension boolean bo1;
        @Extension byte by1;
        @Extension short sh1;
        @Extension int in1;
        @Extension long lo1;
        @Extension float fl1;
        @Extension double du1;

        @Extension Boolean bo2;
        @Extension Byte by2;
        @Extension Short sh2;
        @Extension Integer in2;
        @Extension Long lo2;
        @Extension Float fl2;
        @Extension Double du2;

        @Extension String str;
        @Extension URI uri;
    }

    @Test void shouldBuildCustomTypeWithUnnamedExtensionsFieldSetConverting() {
        givenRegisteredType(CustomWithUnnamedExtensionsException.class);
        entity.add("bo1", "true");
        entity.add("by1", "12");
        entity.add("sh1", "123");
        entity.add("in1", "1234");
        entity.add("lo1", "12345");
        entity.add("fl1", "1.12");
        entity.add("du1", "1.23456");

        entity.add("bo2", "true");
        entity.add("by2", "123");
        entity.add("sh2", "1234");
        entity.add("in2", "12345");
        entity.add("lo2", "123456");
        entity.add("fl2", "1.123");
        entity.add("du2", "1.234567");

        entity.add("str", "dummy-string");
        entity.add("uri", "dummy:uri");

        CustomWithUnnamedExtensionsException thrown = catchThrowableOfType(this::trigger, CustomWithUnnamedExtensionsException.class);

        assertThat(thrown).hasMessage(null);

        assertThat(thrown.bo1).isEqualTo(true);
        assertThat(thrown.by1).isEqualTo((byte) 12);
        assertThat(thrown.sh1).isEqualTo((short) 123);
        assertThat(thrown.in1).isEqualTo(1234);
        assertThat(thrown.lo1).isEqualTo(12345);
        assertThat(thrown.fl1).isEqualTo(1.12f);
        assertThat(thrown.du1).isEqualTo(1.23456D);

        assertThat(thrown.bo2).isEqualTo(true);
        assertThat(thrown.by2).isEqualTo((byte) 123);
        assertThat(thrown.sh2).isEqualTo((short) 1234);
        assertThat(thrown.in2).isEqualTo(12345);
        assertThat(thrown.lo2).isEqualTo(123456);
        assertThat(thrown.fl2).isEqualTo(1.123f);
        assertThat(thrown.du2).isEqualTo(1.234567D);

        assertThat(thrown.str).isEqualTo("dummy-string");
        assertThat(thrown.uri).isEqualTo(URI.create("dummy:uri"));
    }

    @Test void shouldBuildCustomTypeWithUnnamedExtensionsFieldSetNotConverting() {
        givenRegisteredType(CustomWithUnnamedExtensionsException.class);
        entity.add("bo1", true);
        entity.add("by1", 12);
        entity.add("sh1", 123);
        entity.add("in1", 1234);
        entity.add("lo1", 12345);
        entity.add("fl1", 1.12f);
        entity.add("du1", 1.23456d);

        entity.add("bo2", true);
        entity.add("by2", 123);
        entity.add("sh2", 1234);
        entity.add("in2", 12345);
        entity.add("lo2", 123456);
        entity.add("fl2", 1.123f);
        entity.add("du2", 1.234567d);

        CustomWithUnnamedExtensionsException thrown = catchThrowableOfType(this::trigger, CustomWithUnnamedExtensionsException.class);

        assertThat(thrown).hasMessage(null);

        assertThat(thrown.bo1).isEqualTo(true);
        assertThat(thrown.by1).isEqualTo((byte) 12);
        assertThat(thrown.sh1).isEqualTo((short) 123);
        assertThat(thrown.in1).isEqualTo(1234);
        assertThat(thrown.lo1).isEqualTo(12345);
        assertThat(thrown.fl1).isEqualTo(1.12f);
        assertThat(thrown.du1).isEqualTo(1.23456D);

        assertThat(thrown.bo2).isEqualTo(true);
        assertThat(thrown.by2).isEqualTo((byte) 123);
        assertThat(thrown.sh2).isEqualTo((short) 1234);
        assertThat(thrown.in2).isEqualTo(12345);
        assertThat(thrown.lo2).isEqualTo(123456);
        assertThat(thrown.fl2).isEqualTo(1.123f);
        assertThat(thrown.du2).isEqualTo(1.234567D);
    }


    public static class CustomWithNamedExtensionsException extends RuntimeException {
        @Extension("bo1") boolean bo1x;
        @Extension("by1") byte by1x;
        @Extension("sh1") short sh1x;
        @Extension("in1") int in1x;
        @Extension("lo1") long lo1x;
        @Extension("fl1") float fl1x;
        @Extension("du1") double du1x;

        @Extension("bo2") Boolean bo2x;
        @Extension("by2") Byte by2x;
        @Extension("sh2") Short sh2x;
        @Extension("in2") Integer in2x;
        @Extension("lo2") Long lo2x;
        @Extension("fl2") Float fl2x;
        @Extension("du2") Double du2x;

        @Extension("str") String strX;
        @Extension("uri") URI uriX;
    }

    @Test void shouldBuildCustomTypeWithNamedExtensionsFieldSet() {
        givenRegisteredType(CustomWithNamedExtensionsException.class);
        entity.add("bo1", "true");
        entity.add("by1", "12");
        entity.add("sh1", "123");
        entity.add("in1", "1234");
        entity.add("lo1", "12345");
        entity.add("fl1", "1.12");
        entity.add("du1", "1.23456");

        entity.add("bo2", "true");
        entity.add("by2", "123");
        entity.add("sh2", "1234");
        entity.add("in2", "12345");
        entity.add("lo2", "123456");
        entity.add("fl2", "1.123");
        entity.add("du2", "1.234567");

        entity.add("str", "dummy-string");
        entity.add("uri", "dummy:uri");

        CustomWithNamedExtensionsException thrown = catchThrowableOfType(this::trigger, CustomWithNamedExtensionsException.class);

        assertThat(thrown).hasMessage(null);

        assertThat(thrown.bo1x).isEqualTo(true);
        assertThat(thrown.by1x).isEqualTo((byte) 12);
        assertThat(thrown.sh1x).isEqualTo((short) 123);
        assertThat(thrown.in1x).isEqualTo(1234);
        assertThat(thrown.lo1x).isEqualTo(12345);
        assertThat(thrown.fl1x).isEqualTo(1.12f);
        assertThat(thrown.du1x).isEqualTo(1.23456D);

        assertThat(thrown.bo2x).isEqualTo(true);
        assertThat(thrown.by2x).isEqualTo((byte) 123);
        assertThat(thrown.sh2x).isEqualTo((short) 1234);
        assertThat(thrown.in2x).isEqualTo(12345);
        assertThat(thrown.lo2x).isEqualTo(123456);
        assertThat(thrown.fl2x).isEqualTo(1.123f);
        assertThat(thrown.du2x).isEqualTo(1.234567D);

        assertThat(thrown.strX).isEqualTo("dummy-string");
        assertThat(thrown.uriX).isEqualTo(URI.create("dummy:uri"));
    }


    private void trigger() {
        String json = entity.build().toString();
        InputStream inputStream = new ByteArrayInputStream(json.getBytes(UTF_8));
        new ProblemDetailJsonToExceptionBuilder(inputStream).trigger();
    }

    private void givenRegisteredType(Class<? extends RuntimeException> type) {
        URI typeUri = ProblemDetailExceptionRegistry.register(type);
        entity.add("type", typeUri.toString());
    }

    private static final URI SOME_URI = URI.create("some:uri");
}
