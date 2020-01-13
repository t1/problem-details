# 3. Register into JAX-RS server as ExceptionMapper

Date: 2020-01-12

## Status

Accepted

## Context

Intercept problem detail responses received on the JAX-RS client side and convert them into exceptions.

## Decision

Register a `ClientResponseFilter` with JAX-RS to throw an exception whenever a problem detail response is found; convert the body back to an exception and throw it.

## Consequences

JAX-RS wraps all exceptions thrown by a `ClientResponseFilter` in a `ResponseProcessingException`. This has to be unwrapped by the application code.

The application code also has to register all exceptions to be mapped to.
