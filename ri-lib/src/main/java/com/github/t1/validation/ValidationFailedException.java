package com.github.t1.validation;

import com.github.t1.problemdetail.Detail;
import com.github.t1.problemdetail.Extension;
import com.github.t1.problemdetail.Status;
import com.github.t1.problemdetail.Title;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static java.util.stream.Collectors.toMap;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

/**
 * Can be mapped as <code>problem detail</code>, exposing only the violations, but not the
 * actual data. You'll find the data together with the stack trace in the logs.
 */
@Title("Validation Failed")
@Status(BAD_REQUEST)
public class ValidationFailedException extends RuntimeException {
    public static void validate(Object object, Class<?>... groups) {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<Object>> violations = validator.validate(object, groups);
        if (violations.isEmpty())
            return;
        throw new ValidationFailedException(violations);
    }

    private final Set<ConstraintViolation<Object>> violations;

    public ValidationFailedException(Set<ConstraintViolation<Object>> violations) {
        super(violations.size() + " violations failed on " + rootBean(violations));
        this.violations = violations;
    }

    // don't expose the message:
    @Detail String detail() {
        return violations.size() + " violations failed";
    }

    @Extension
    public Map<String, String> violations() {
        return violations.stream()
            .map(violation -> new SimpleEntry<>(violation.getPropertyPath().toString(), violation.getMessage()))
            .collect(toMap(Entry::getKey, Entry::getValue));
    }

    private static String rootBean(Set<ConstraintViolation<Object>> violations) {
        return (violations.isEmpty()) ? null : violations.iterator().next().getRootBean().toString();
    }
}
