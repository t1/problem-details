package com.github.t1.validation;

import com.github.t1.problemdetail.Detail;
import com.github.t1.problemdetail.Extension;
import com.github.t1.problemdetail.Status;
import com.github.t1.problemdetail.Title;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toSet;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

/**
 * Can be mapped as <code>problem detail</code>, exposing only the violations, but not the
 * actual data. You'll find the data together with the stack trace in the logs.
 */
@Title("Validation Failed")
@Status(BAD_REQUEST)
public class ValidationFailedException extends RuntimeException {

    private static ValidatorFactory VALIDATOR_FACTORY;

    public static void validate(Object object, Class<?>... groups) {
        if (VALIDATOR_FACTORY == null) VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();
        validate(VALIDATOR_FACTORY, object, groups);
    }

    public static void validate(ValidatorFactory factory, Object object, Class<?>... groups) {
        Set<ConstraintViolation<Object>> violations = factory.getValidator().validate(object, groups);
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
    public Map<String, Set<String>> violations() {
        return violations.stream().collect(groupingBy(
            (ConstraintViolation<?> violation) -> violation.getPropertyPath().toString(),
            mapping(ConstraintViolation::getMessage, toSet())
        ));
    }

    private static String rootBean(Set<ConstraintViolation<Object>> violations) {
        return (violations.isEmpty()) ? null : violations.iterator().next().getRootBean().toString();
    }
}
