package test;

import com.github.t1.problemdetail.Logging;
import com.github.t1.problemdetail.Status;
import com.github.t1.problemdetail.ri.lib.ProblemDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test.sub.SubException;
import test.sub.SubExceptionWithCategory;
import test.sub.SubExceptionWithLevel;

import java.net.URI;

import static com.github.t1.problemdetail.LogLevel.DEBUG;
import static com.github.t1.problemdetail.LogLevel.ERROR;
import static com.github.t1.problemdetail.LogLevel.INFO;
import static com.github.t1.problemdetail.LogLevel.OFF;
import static com.github.t1.problemdetail.LogLevel.WARNING;
import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static org.mockito.BDDMockito.then;
import static test.MockLoggerFactory.onlyLogger;

class LoggingBehavior {
    @BeforeEach void setUp() { MockLoggerFactory.reset(); }

    @Test void shouldLogAuto5xxAtError() {
        @Status(INTERNAL_SERVER_ERROR) class CustomException extends Exception {}

        ProblemDetails details = new MockProblemDetails(new CustomException());

        then(onlyLogger(CustomException.class)).should().error(details.getLogMessage());
    }

    @Test void shouldLogAuto4xxAtDebug() {
        @Status(BAD_REQUEST) class CustomException extends Exception {}

        ProblemDetails details = new MockProblemDetails(new CustomException());

        then(onlyLogger(CustomException.class)).should().debug(details.getLogMessage());
    }

    @Test void shouldLogExplicitlyAtError() {
        @Logging(at = ERROR) class CustomException extends Exception {}

        ProblemDetails details = new MockProblemDetails(new CustomException());

        then(onlyLogger(CustomException.class)).should().error(details.getLogMessage());
    }

    @Test void shouldLogExplicitlyAtWarning() {
        @Logging(at = WARNING) class CustomException extends Exception {}

        ProblemDetails details = new MockProblemDetails(new CustomException());

        then(onlyLogger(CustomException.class)).should().warn(details.getLogMessage());
    }

    @Test void shouldLogExplicitlyAtInfo() {
        @Logging(at = INFO) class CustomException extends Exception {}

        ProblemDetails details = new MockProblemDetails(new CustomException());

        then(onlyLogger(CustomException.class)).should().info(details.getLogMessage());
    }

    @Test void shouldLogExplicitlyAtDebug() {
        @Logging(at = DEBUG) class CustomException extends Exception {}

        ProblemDetails details = new MockProblemDetails(new CustomException());

        then(onlyLogger(CustomException.class)).should().debug(details.getLogMessage());
    }

    @Test void shouldLogExplicitlyAtOff() {
        @Logging(at = OFF) class CustomException extends Exception {}

        new MockProblemDetails(new CustomException());

        then(onlyLogger(CustomException.class)).shouldHaveNoInteractions();
    }


    @Test void shouldLogToExplicitCategory() {
        @Logging(to = "my-errors") class CustomException extends Exception {}

        ProblemDetails details = new MockProblemDetails(new CustomException());

        then(onlyLogger("my-errors")).should().error(details.getLogMessage());
    }


    @Test void shouldLogToPackageAnnotatedCategory() {
        ProblemDetails details = new MockProblemDetails(new SubException());

        then(onlyLogger("warnings")).should().warn(details.getLogMessage());
    }

    @Test void shouldOverridePackageAnnotatedLogLevel() {
        ProblemDetails details = new MockProblemDetails(new SubExceptionWithLevel());

        then(onlyLogger("warnings")).should().info(details.getLogMessage());
    }

    @Test void shouldOverridePackageAnnotatedLogCategory() {
        ProblemDetails details = new MockProblemDetails(new SubExceptionWithCategory());

        then(onlyLogger("sub-cat")).should().warn(details.getLogMessage());
    }

    private static class MockProblemDetails extends ProblemDetails {
        public MockProblemDetails(Exception exception) { super(exception); }

        @Override protected boolean hasDefaultMessage() { return false; }

        @Override protected String findMediaTypeSubtype() { return "json"; }

        @Override protected URI buildInstance() { return URI.create("urn:some-instance"); }
    }
}
