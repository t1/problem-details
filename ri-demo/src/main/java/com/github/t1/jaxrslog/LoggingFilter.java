package com.github.t1.jaxrslog;

import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.net.URI;
import java.util.function.Consumer;

@Slf4j
@Provider
@PreMatching
@SuppressWarnings("unused")
public class LoggingFilter implements
    ContainerRequestFilter, ContainerResponseFilter,
    ClientRequestFilter, // TODO RestEasy warns: RESTEASY002190: Annotation, @PreMaching, not valid on ClientRequestFilter
    ClientResponseFilter {

    public static LoggingFilter toStdOut() {
        return new LoggingFilter(System.out::print);
    }

    public static LoggingFilter toStdErr() {
        return new LoggingFilter(System.err::print);
    }

    public static LoggingFilter atInfo() {
        return new LoggingFilter(log::info);
    }

    public static LoggingFilter atDebug() {
        return new LoggingFilter(log::debug);
    }

    @SuppressWarnings("unused") public LoggingFilter() {
        this(log::debug);
    }

    public LoggingFilter(Consumer<String> messageConsumer) {
        this(messageConsumer, null);
    }

    public LoggingFilter(Consumer<String> messageConsumer, UriInfo uriInfo) {
        this.messageConsumer = messageConsumer;
        this.uriInfo = uriInfo;
    }

    private final Consumer<String> messageConsumer;

    @Context UriInfo uriInfo;

    /* container request */
    @Override public void filter(ContainerRequestContext requestContext) {
        new MessageBuilder("--> ", requestContext.getMethod(), requestContext.getUriInfo().getRequestUri(), null,
            requestContext.getHeaders())
            .log();
    }

    /* container response */
    @Override public void filter(ContainerRequestContext request, ContainerResponseContext response) {
        new MessageBuilder("<-- ", request.getMethod(), request.getUriInfo().getRequestUri(), response.getStatusInfo(),
            response.getHeaders())
            .log();
    }

    /* client request */
    @Override public void filter(ClientRequestContext request) {
        new MessageBuilder("==> ", request.getMethod(), request.getUri(), null, request.getHeaders())
            .log();
    }

    /* client response */
    @Override public void filter(ClientRequestContext request, ClientResponseContext response) {
        new MessageBuilder("<== " + response.getStatusInfo(), request.getMethod(), request.getUri(), response.getStatusInfo(),
            response.getHeaders())
            .log();
    }

    private class MessageBuilder {
        private final StringBuilder out = new StringBuilder();
        private final String prefix;

        public MessageBuilder(String prefix, String method, URI uri, StatusType statusInfo, MultivaluedMap<String, ?> headers) {
            this.prefix = prefix;

            printStatus(method, uri, statusInfo);
            printHeaders(headers);
        }

        private void printStatus(String method, URI uri, StatusType statusInfo) {
            if (uriInfo != null)
                try {
                    uri = uriInfo.getBaseUri().relativize(uri);
                } catch (Exception e) {
                    // ignore
                }
            out.append(prefix).append(method).append(' ');
            if (!uri.isAbsolute())
                out.append('/');
            out.append(uri);
            if (statusInfo != null)
                out.append(" :: ").append(statusInfo.getStatusCode()).append(" ").append(statusInfo.getReasonPhrase());
            out.append('\n');
        }

        private void printHeaders(MultivaluedMap<String, ?> headers) {
            for (String key : headers.keySet()) {
                out.append(prefix).append("  ").append(key).append(": ");
                for (Object value : headers.get(key)) {
                    out.append((value == null) ? null : value.toString());
                }
                out.append("\n");
            }
        }

        public void log() {
            messageConsumer.accept(out.toString());
        }
    }

}
