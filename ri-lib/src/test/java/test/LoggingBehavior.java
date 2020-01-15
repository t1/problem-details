package test;

import com.github.t1.problemdetail.Logging;
import com.github.t1.problemdetail.Status;
import com.github.t1.problemdetail.ri.lib.ProblemDetailBuilder;
import org.junit.jupiter.api.AfterEach;
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
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static test.MockLoggerFactory.onlyLogger;

class LoggingBehavior {
    @AfterEach void cleanup() { MockLoggerFactory.reset(); }

    @Test void shouldLogAuto5xxAtError() {
        @Status(INTERNAL_SERVER_ERROR) class CustomException extends Exception {}

        ProblemDetailBuilder details = problemDetailsFor(new CustomException());

        then(onlyLogger(CustomException.class)).should().error(eq(details.getLogMessage()), any(CustomException.class));
    }

    @Test void shouldLogAuto4xxAtDebug() {
        @Status(BAD_REQUEST) class CustomException extends Exception {}

        ProblemDetailBuilder details = problemDetailsFor(new CustomException());

        then(onlyLogger(CustomException.class)).should().debug(details.getLogMessage());
    }

    @Test void shouldLogExplicitlyAtError() {
        @Logging(at = ERROR) class CustomException extends Exception {}

        ProblemDetailBuilder details = problemDetailsFor(new CustomException());

        then(onlyLogger(CustomException.class)).should().error(eq(details.getLogMessage()), any(CustomException.class));
    }

    @Test void shouldLogExplicitlyAtWarning() {
        @Logging(at = WARNING) class CustomException extends Exception {}

        ProblemDetailBuilder details = problemDetailsFor(new CustomException());

        then(onlyLogger(CustomException.class)).should().warn(eq(details.getLogMessage()), any(CustomException.class));
    }

    @Test void shouldLogExplicitlyAtInfo() {
        @Logging(at = INFO) class CustomException extends Exception {}

        ProblemDetailBuilder details = problemDetailsFor(new CustomException());

        then(onlyLogger(CustomException.class)).should().info(details.getLogMessage());
    }

    @Test void shouldLogExplicitlyAtDebug() {
        @Logging(at = DEBUG) class CustomException extends Exception {}

        ProblemDetailBuilder details = problemDetailsFor(new CustomException());

        then(onlyLogger(CustomException.class)).should().debug(details.getLogMessage());
    }

    @Test void shouldLogExplicitlyAtOff() {
        @Logging(at = OFF) class CustomException extends Exception {}

        problemDetailsFor(new CustomException());

        then(onlyLogger(CustomException.class)).shouldHaveNoInteractions();
    }


    @Test void shouldLogToExplicitCategory() {
        @Logging(to = "my-errors") class CustomException extends Exception {}

        ProblemDetailBuilder details = problemDetailsFor(new CustomException());

        then(onlyLogger("my-errors")).should().error(eq(details.getLogMessage()), any(CustomException.class));
    }


    @Test void shouldLogToPackageAnnotatedCategory() {
        ProblemDetailBuilder details = problemDetailsFor(new SubException());

        then(onlyLogger("warnings")).should().warn(eq(details.getLogMessage()), any(SubException.class));
    }

    @Test void shouldOverridePackageAnnotatedLogLevel() {
        ProblemDetailBuilder details = problemDetailsFor(new SubExceptionWithLevel());

        then(onlyLogger("warnings")).should().info(details.getLogMessage());
    }

    @Test void shouldOverridePackageAnnotatedLogCategory() {
        ProblemDetailBuilder details = problemDetailsFor(new SubExceptionWithCategory());

        then(onlyLogger("sub-cat")).should().warn(eq(details.getLogMessage()), any(SubExceptionWithCategory.class));
    }

    private MockProblemDetailBuilder problemDetailsFor(Exception exception) {
        MockProblemDetailBuilder problemDetails = new MockProblemDetailBuilder(exception);
        problemDetails.log();
        return problemDetails;
    }

    private static class MockProblemDetailBuilder extends ProblemDetailBuilder {
        public MockProblemDetailBuilder(Exception exception) { super(exception); }

        @Override protected boolean hasDefaultMessage() { return false; }

        @Override protected String findMediaTypeSubtype() { return "json"; }

        @Override protected URI buildInstance() { return URI.create("urn:some-instance"); }
    }
}
