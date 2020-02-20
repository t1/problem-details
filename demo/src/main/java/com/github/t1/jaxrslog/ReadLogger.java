package com.github.t1.jaxrslog;

import lombok.extern.java.Log;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;
import java.io.IOException;

// TODO RestEasy: this is never called, not even when manually registered
@Log
@Provider
public class ReadLogger implements ReaderInterceptor {
    @Override public Object aroundReadFrom(ReaderInterceptorContext context) throws IOException, WebApplicationException {
        log.info("around read from context");
        return context.proceed();
    }
}
