package com.github.t1.problemdetail.ri;

import com.github.t1.validation.ValidationFailedException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.Set;

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

        return new JaxRsProblemDetails(validationFailedException, requestHeaders)
            .log()
            .toResponse();
    }
}
