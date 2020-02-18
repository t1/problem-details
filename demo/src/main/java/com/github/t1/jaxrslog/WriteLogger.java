package com.github.t1.jaxrslog;

import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.eclipse.microprofile.problemdetails.Constants.PROBLEM_DETAIL_JSON;

// TODO RestEasy: this is not registered when it's implemented directly by the LoggingFilter
@Slf4j
@Provider
public class WriteLogger implements WriterInterceptor {
    public static final MediaType PROBLEM_DETAIL_JSON_TYPE = MediaType.valueOf(PROBLEM_DETAIL_JSON);

    @Override public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
        if (PROBLEM_DETAIL_JSON_TYPE.isCompatible(contentType(context))) {
            LoggingOutputStream buffer = new LoggingOutputStream(context.getOutputStream());
            context.setOutputStream(buffer);
            context.proceed();
            log.info("<-- {}", buffer.copy.toString());
        } else {
            context.proceed();
        }
    }

    private MediaType contentType(WriterInterceptorContext context) {
        Object header = context.getHeaders().getFirst("Content-Type");
        return (header == null) ? null : MediaType.valueOf(header.toString());
    }

    private static class LoggingOutputStream extends FilterOutputStream {
        private final ByteArrayOutputStream copy = new ByteArrayOutputStream();

        public LoggingOutputStream(OutputStream out) { super(out); }

        @Override public void write(int b) throws IOException {
            super.write(b);
            copy.write(b);
        }
    }
}
