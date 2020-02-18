# 4. Register into MP Rest Client as ResponseExceptionMapper

Date: 2020-01-12

## Status

Accepted

## Context

Intercept problem detail responses received on the MP client side and convert them into exceptions.

## Decision

Register a `ResponseExceptionMapper` with MP Rest Client by annotating it as `@Provider` to produce `Throwable`s.

Scan the stack trace for the first proxy, which is the MP Rest Client api interface and register all exceptions on all methods.

## Consequences

The application code for the MP Rest Client API has to be annotated with the exceptions that can be thrown.

The exception scanning heuristic may break any time.
