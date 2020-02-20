package test;

import com.github.t1.problemdetails.jaxrs.lib.ProblemDetailBuilder;
import org.eclipse.microprofile.problemdetails.Logging;
import org.eclipse.microprofile.problemdetails.Status;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import test.JavaUtilLoggingMemento.Log;
import test.sub.SubException;
import test.sub.SubExceptionWithCategory;
import test.sub.SubExceptionWithLevel;

import java.net.URI;
import java.util.logging.Level;

import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import static org.assertj.core.api.BDDAssertions.then;
import static org.eclipse.microprofile.problemdetails.LogLevel.DEBUG;
import static org.eclipse.microprofile.problemdetails.LogLevel.ERROR;
import static org.eclipse.microprofile.problemdetails.LogLevel.INFO;
import static org.eclipse.microprofile.problemdetails.LogLevel.OFF;
import static org.eclipse.microprofile.problemdetails.LogLevel.WARN;
import static org.eclipse.microprofile.problemdetails.ResponseStatus.BAD_REQUEST;
import static org.eclipse.microprofile.problemdetails.ResponseStatus.INTERNAL_SERVER_ERROR;
import static test.JavaUtilLoggingMemento.LOGS;

@ExtendWith(JavaUtilLoggingMemento.class)
class LoggingBehavior {
    @Test void shouldLogAuto5xxAtError() {
        @Status(INTERNAL_SERVER_ERROR) class CustomException extends Exception {}
        CustomException exception = new CustomException();

        ProblemDetailBuilder details = problemDetailsFor(exception);

        then(LOGS).containsExactly(new Log(CustomException.class.getName(), SEVERE, exception, details.getLogMessage()));
    }

    @Test void shouldLogAuto4xxAtDebug() {
        @Status(BAD_REQUEST) class CustomException extends Exception {}
        CustomException exception = new CustomException();

        ProblemDetailBuilder details = problemDetailsFor(exception);

        then(LOGS).containsExactly(new Log(CustomException.class.getName(), Level.INFO, null, details.getLogMessage()));
    }

    @Test void shouldLogExplicitlyAtError() {
        @Logging(at = ERROR) class CustomException extends Exception {}
        CustomException exception = new CustomException();

        ProblemDetailBuilder details = problemDetailsFor(exception);

        then(LOGS).containsExactly(new Log(CustomException.class.getName(), SEVERE, exception, details.getLogMessage()));
    }

    @Test void shouldLogExplicitlyAtWarning() {
        @Logging(at = WARN) class CustomException extends Exception {}
        CustomException exception = new CustomException();

        ProblemDetailBuilder details = problemDetailsFor(exception);

        then(LOGS).containsExactly(new Log(CustomException.class.getName(), WARNING, exception, details.getLogMessage()));
    }

    @Test void shouldLogExplicitlyAtInfo() {
        @Logging(at = INFO) class CustomException extends Exception {}

        ProblemDetailBuilder details = problemDetailsFor(new CustomException());

        then(LOGS).containsExactly(new Log(CustomException.class.getName(), Level.INFO, null, details.getLogMessage()));
    }

    @Test void shouldLogExplicitlyAtDebug() {
        @Logging(at = DEBUG) class CustomException extends Exception {}

        ProblemDetailBuilder details = problemDetailsFor(new CustomException());

        then(LOGS).containsExactly(new Log(CustomException.class.getName(), Level.FINE, null, details.getLogMessage()));
    }

    @Test void shouldLogExplicitlyAtOff() {
        @Logging(at = OFF) class CustomException extends Exception {}

        problemDetailsFor(new CustomException());

        then(LOGS).isEmpty();
    }


    @Test void shouldLogToExplicitCategory() {
        @Logging(to = "my-errors") class CustomException extends Exception {}

        ProblemDetailBuilder details = problemDetailsFor(new CustomException());

        then(LOGS).containsExactly(new Log("my-errors", Level.INFO, null, details.getLogMessage()));
    }


    @Test void shouldLogToPackageAnnotatedCategory() {
        SubException exception = new SubException();

        ProblemDetailBuilder details = problemDetailsFor(exception);

        then(LOGS).containsExactly(new Log("warnings", WARNING, exception, details.getLogMessage()));
    }

    @Test void shouldOverridePackageAnnotatedLogLevel() {
        ProblemDetailBuilder details = problemDetailsFor(new SubExceptionWithLevel());

        then(LOGS).containsExactly(new Log("warnings", Level.INFO, null, details.getLogMessage()));
    }

    @Test void shouldOverridePackageAnnotatedLogCategory() {
        SubExceptionWithCategory exception = new SubExceptionWithCategory();

        ProblemDetailBuilder details = problemDetailsFor(exception);

        then(LOGS).containsExactly(new Log("sub-cat", WARNING, exception, details.getLogMessage()));
    }

    private MockProblemDetailBuilder problemDetailsFor(Exception exception) {
        MockProblemDetailBuilder problemDetails = new MockProblemDetailBuilder(exception);
        problemDetails.log();
        return problemDetails;
    }

    private static class MockProblemDetailBuilder extends ProblemDetailBuilder {
        public MockProblemDetailBuilder(Exception exception) { super(exception); }

        @Override protected boolean useExceptionMessageAsDetail() { return true; }

        @Override protected String findMediaTypeSubtype() { return "json"; }

        @Override protected URI buildInstance() { return URI.create("urn:some-instance"); }
    }
}
