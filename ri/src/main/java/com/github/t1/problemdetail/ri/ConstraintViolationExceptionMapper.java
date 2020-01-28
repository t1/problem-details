package com.github.t1.problemdetail.ri;

import org.eclipse.microprofile.problemdetails.Detail;
import org.eclipse.microprofile.problemdetails.Extension;
import org.eclipse.microprofile.problemdetails.Status;
import org.eclipse.microprofile.problemdetails.Title;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static java.util.stream.Collectors.toMap;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

/**
 * Separate exception mapper for {@link ConstraintViolationException}s, as the
 * generic exception mapper for Throwable is too generic to be called by JAX-RS.
 */
@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {
    @Context HttpHeaders requestHeaders;

    @Override
    public Response toResponse(ConstraintViolationException constraintViolationException) {
        Set<ConstraintViolation<?>> violations = constraintViolationException.getConstraintViolations();
        ValidationFailedException validationFailedException = new ValidationFailedException(violations);

        return new JaxRsProblemDetailBuilder(validationFailedException, requestHeaders)
            .log()
            .toResponse();
    }

    @Title("Validation Failed")
    @Status(BAD_REQUEST)
    public static class ValidationFailedException extends ConstraintViolationException {
        public ValidationFailedException(Set<ConstraintViolation<?>> violations) {
            super(violations);
        }

        // don't expose the message:
        @Detail String detail() {
            return getConstraintViolations().size() + " violations failed";
        }

        @Extension Map<String, String> violations() {
            return getConstraintViolations().stream()
                .map(violation -> new SimpleEntry<>(violation.getPropertyPath().toString(), violation.getMessage()))
                .collect(toMap(Entry::getKey, Entry::getValue));
        }
    }
}
