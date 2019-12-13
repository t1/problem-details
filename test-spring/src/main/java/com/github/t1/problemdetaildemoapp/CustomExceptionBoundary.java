package com.github.t1.problemdetaildemoapp;

import com.github.t1.problemdetail.Detail;
import com.github.t1.problemdetail.Extension;
import com.github.t1.problemdetail.Instance;
import com.github.t1.problemdetail.Status;
import com.github.t1.problemdetail.Title;
import com.github.t1.problemdetail.Type;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;

@RestController
@RequestMapping(path = "/custom")
public class CustomExceptionBoundary {
    @PostMapping(path = "/runtime-exception")
    public void customRuntimeException() {
        class CustomException extends RuntimeException {}
        throw new CustomException();
    }

    @PostMapping(path = "/illegal-argument-exception")
    public void customIllegalArgumentException() {
        class CustomException extends IllegalArgumentException {}
        throw new CustomException();
    }

    @PostMapping(path = "explicit-type")
    public void customTypeException() {
        @Type("http://error-codes.org/out-of-memory")
        class SomeException extends RuntimeException {}
        throw new SomeException();
    }

    @PostMapping(path = "explicit-title")
    public void customTitleException() {
        @Title("Some Title")
        class SomeException extends RuntimeException {}
        throw new SomeException();
    }

    @PostMapping(path = "explicit-status")
    public void customExplicitStatus() {
        @Status(FORBIDDEN)
        class SomethingForbiddenException extends RuntimeException {}
        throw new SomethingForbiddenException();
    }

    @PostMapping(path = "public-detail-method")
    public void publicDetailMethod() {
        class SomeMessageException extends RuntimeException {
            @Detail public String detail() { return "some detail"; }
        }
        throw new SomeMessageException();
    }

    @PostMapping(path = "private-detail-method")
    public void privateDetailMethod() {
        class SomeMessageException extends RuntimeException {
            @Detail private String detail() { return "some detail"; }
        }
        throw new SomeMessageException();
    }

    @PostMapping(path = "failing-detail-method")
    public void failingDetailMethod() {
        class FailingDetailException extends RuntimeException {
            public FailingDetailException() { super("some message"); }

            @Detail public String failingDetail() {
                throw new RuntimeException("inner");
            }
        }
        throw new FailingDetailException();
    }

    @PostMapping(path = "public-detail-field")
    public void publicDetailField() {
        class SomeMessageException extends RuntimeException {
            @Detail public String detail = "some detail";

            public SomeMessageException(String message) {
                super(message);
            }
        }
        throw new SomeMessageException("overwritten");
    }

    @PostMapping(path = "private-detail-field")
    public void privateDetailField() {
        class SomeMessageException extends RuntimeException {
            @Detail private String detail = "some detail";

            public SomeMessageException(String message) {
                super(message);
            }
        }
        throw new SomeMessageException("overwritten");
    }

    @PostMapping(path = "multi-detail-fields")
    public void multiDetailField() {
        class SomeMessageException extends RuntimeException {
            @Detail public String detail1 = "detail a";
            @Detail public String detail2 = "detail b";
        }
        throw new SomeMessageException();
    }

    @PostMapping(path = "mixed-details")
    public void multiDetails() {
        class SomeMessageException extends RuntimeException {
            @Detail public String detail0() { return "detail a"; }

            @Detail public String detail1 = "detail b";
            @Detail public String detail2 = "detail c";
        }
        throw new SomeMessageException();
    }

    @PostMapping(path = "detail-method-arg")
    public void detailMethodArg() {
        class SomeMessageException extends RuntimeException {
            @Detail public String detail(String foo) { return "some " + foo; }
        }
        throw new SomeMessageException();
    }

    @PostMapping(path = "explicit-uri-instance")
    public void customInstanceException() {
        class SomeException extends RuntimeException {
            @Instance URI instance() { return URI.create("foobar"); }
        }
        throw new SomeException();
    }

    @PostMapping(path = "extension-method")
    public void customExtensionMethod() {
        class SomeException extends RuntimeException {
            @Extension public String ex() { return "some extension"; }
        }
        throw new SomeException();
    }

    @PostMapping(path = "extension-method-with-name")
    public void customExtensionMethodWithExplicitName() {
        class SomeMessageException extends RuntimeException {
            @Extension("foo") public String ex() { return "some extension"; }
        }
        throw new SomeMessageException();
    }

    @PostMapping(path = "extension-field")
    public void customExtensionField() {
        class SomeMessageException extends RuntimeException {
            @Extension public String ex = "some extension";
        }
        throw new SomeMessageException();
    }

    @PostMapping(path = "extension-field-with-name")
    public void customExtensionFieldWithName() {
        class SomeMessageException extends RuntimeException {
            @Extension("foo") public String ex = "some extension";
        }
        throw new SomeMessageException();
    }

    @PostMapping(path = "multi-extension")
    public void multiExtension() {
        class SomeMessageException extends RuntimeException {
            @Extension String m1() { return "method 1"; }

            @Extension("m2") String method() { return "method 2"; }

            @Extension String f1 = "field 1";
            @Extension("f2") String field = "field 2";
        }
        throw new SomeMessageException();
    }
}
