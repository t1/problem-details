# 2. Register into JAX-RS server as ClientResponseFilter

Date: 2020-01-12

## Status

Accepted

## Context

Convert exceptions thrown on the JAX-RS server side into Response objects with problem detail bodies.

## Decision

Register an `ExceptionMapper` in JAX-RS by annotating it as `@Provider` for `Throwable`s, and a second one for `ConstraintViolationException`s to convert Bean Validation errors produced by `@Valid` annotated parameters.

## Consequences

No known side-effects.
