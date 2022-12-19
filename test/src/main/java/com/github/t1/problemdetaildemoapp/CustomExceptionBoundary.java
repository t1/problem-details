package com.github.t1.problemdetaildemoapp;

import com.github.t1.problemdetail.Detail;
import com.github.t1.problemdetail.Extension;
import com.github.t1.problemdetail.Instance;
import com.github.t1.problemdetail.Status;
import com.github.t1.problemdetail.Title;
import com.github.t1.problemdetail.Type;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import java.net.URI;

import static jakarta.ws.rs.core.Response.Status.FORBIDDEN;

@Path("/custom")
public class CustomExceptionBoundary {
    @Path("/runtime-exception")
    @POST public void customRuntimeException() {
        class CustomException extends RuntimeException {}
        throw new CustomException();
    }

    @Path("/illegal-argument-exception")
    @POST public void customIllegalArgumentException() {
        class CustomException extends IllegalArgumentException {}
        throw new CustomException();
    }

    @Path("/explicit-type")
    @POST public void customTypeException() {
        @Type("https://error-codes.org/out-of-memory")
        class SomeException extends RuntimeException {}
        throw new SomeException();
    }

    @Path("/explicit-title")
    @POST public void customTitleException() {
        @Title("Some Title")
        class SomeException extends RuntimeException {}
        throw new SomeException();
    }

    @Path("/explicit-status")
    @POST public void customExplicitStatus() {
        @Status(FORBIDDEN)
        class SomethingForbiddenException extends RuntimeException {}
        throw new SomethingForbiddenException();
    }

    @Path("/public-detail-method")
    @POST public void publicDetailMethod() {
        class SomeMessageException extends RuntimeException {
            @Detail public String detail() { return "some detail"; }
        }
        throw new SomeMessageException();
    }

    @Path("/private-detail-method")
    @POST public void privateDetailMethod() {
        class SomeMessageException extends RuntimeException {
            @Detail private String detail() { return "some detail"; }
        }
        throw new SomeMessageException();
    }

    @Path("/failing-detail-method")
    @POST public void failingDetailMethod() {
        class FailingDetailException extends RuntimeException {
            public FailingDetailException() { super("some message"); }

            @Detail public String failingDetail() {
                throw new RuntimeException("inner");
            }
        }
        throw new FailingDetailException();
    }

    @Path("/public-detail-field")
    @POST public void publicDetailField() {
        class SomeMessageException extends RuntimeException {
            @Detail public String detail = "some detail";

            public SomeMessageException(String message) {
                super(message);
            }
        }
        throw new SomeMessageException("overwritten");
    }

    @Path("/private-detail-field")
    @POST public void privateDetailField() {
        class SomeMessageException extends RuntimeException {
            @Detail private final String detail = "some detail";

            public SomeMessageException(String message) {
                super(message);
            }
        }
        throw new SomeMessageException("overwritten");
    }

    @Path("/multi-detail-fields")
    @POST public void multiDetailField() {
        class SomeMessageException extends RuntimeException {
            @Detail public final String detail1 = "detail a";
            @Detail public final String detail2 = "detail b";
        }
        throw new SomeMessageException();
    }

    @Path("/mixed-details")
    @POST public void multiDetails() {
        class SomeMessageException extends RuntimeException {
            @Detail public String detail0() { return "detail a"; }

            @Detail public final String detail1 = "detail b";
            @Detail public final String detail2 = "detail c";
        }
        throw new SomeMessageException();
    }

    @Path("/detail-method-arg")
    @POST public void detailMethodArg() {
        class SomeMessageException extends RuntimeException {
            @Detail public String detail(String foo) { return "some " + foo; }
        }
        throw new SomeMessageException();
    }

    @Path("/explicit-uri-instance")
    @POST public void customInstanceException() {
        class SomeException extends RuntimeException {
            @Instance URI instance() { return URI.create("foobar"); }
        }
        throw new SomeException();
    }

    @Path("/extension-method")
    @POST public void customExtensionMethod() {
        class SomeException extends RuntimeException {
            @Extension public String ex() { return "some extension"; }
        }
        throw new SomeException();
    }

    @Path("/extension-method-with-name")
    @POST public void customExtensionMethodWithExplicitName() {
        class SomeMessageException extends RuntimeException {
            @Extension("foo") public String ex() { return "some extension"; }
        }
        throw new SomeMessageException();
    }

    @Path("/extension-field")
    @POST public void customExtensionField() {
        class SomeMessageException extends RuntimeException {
            @Extension public final String ex = "some extension";
        }
        throw new SomeMessageException();
    }

    @Path("/extension-field-with-name")
    @POST public void customExtensionFieldWithName() {
        class SomeMessageException extends RuntimeException {
            @Extension("foo") public final String ex = "some extension";
        }
        throw new SomeMessageException();
    }

    @Path("/multi-extension")
    @POST public void multiExtension() {
        class SomeMessageException extends RuntimeException {
            @Extension String m1() { return "method 1"; }

            @Extension("m2") String method() { return "method 2"; }

            @Extension final String f1 = "field 1";
            @Extension("f2") final String field = "field 2";
        }
        throw new SomeMessageException();
    }
}
