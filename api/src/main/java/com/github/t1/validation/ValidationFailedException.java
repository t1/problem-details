package com.github.t1.validation;

import com.github.t1.problemdetail.Detail;
import com.github.t1.problemdetail.Extension;
import com.github.t1.problemdetail.Status;
import com.github.t1.problemdetail.Title;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static java.util.stream.Collectors.toMap;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

/**
 * A variation of the {@link javax.validation.ConstraintViolationException}
 * that can be mapped to a <code>problem detail</code>, exposing only the violations, but not the
 * actual data. You'll find the data together with the stack trace in the logs.
 * <p>
 * As a convenience function, simply call {@link #validate(Object, Class[])}.
 */
@Title("Validation Failed")
@Status(BAD_REQUEST)
public class ValidationFailedException extends ConstraintViolationException {
    private static Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    public static void validate(Object object, Class<?>... groups) {
        @SuppressWarnings({"unchecked", "rawtypes"})
        Set<ConstraintViolation<?>> violations = (Set) VALIDATOR.validate(object, groups);
        if (violations.isEmpty())
            return;
        throw new ValidationFailedException(violations);
    }

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
