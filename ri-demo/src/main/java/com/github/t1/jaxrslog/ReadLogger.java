package com.github.t1.jaxrslog;

import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;
import java.io.IOException;

// TODO RestEasy: this is never called, not even when manually registered
@Slf4j
@Provider
public class ReadLogger implements ReaderInterceptor {
    @Override public Object aroundReadFrom(ReaderInterceptorContext context) throws IOException, WebApplicationException {
        log.error("around read");
        return context.proceed();
    }
}
