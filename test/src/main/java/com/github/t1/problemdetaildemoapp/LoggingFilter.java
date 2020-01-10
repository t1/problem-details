package com.github.t1.problemdetaildemoapp;

import lombok.With;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_XML_TYPE;

@Slf4j
@Provider
@PreMatching
@SuppressWarnings("unused")
public class LoggingFilter implements
    ContainerRequestFilter, ContainerResponseFilter,
    ClientRequestFilter, ClientResponseFilter {

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
        this(false, messageConsumer, null);
    }

    public LoggingFilter(boolean entities, Consumer<String> messageConsumer, UriInfo uriInfo) {
        this.entities = entities;
        this.messageConsumer = messageConsumer;
        this.uriInfo = uriInfo;
    }

    /** Should the request/response body be logged? */
    @With private final boolean entities;

    private final Consumer<String> messageConsumer;

    @Context UriInfo uriInfo;

    /* container request */
    @Override public void filter(ContainerRequestContext requestContext) {
        new MessageBuilder("--> ", requestContext.getMethod(), requestContext.getUriInfo().getRequestUri(), null,
            requestContext.getHeaders())
            .log();
        messageConsumer.accept(entity(requestContext));
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

    private String entity(ContainerRequestContext request) {
        MediaType mediaType = request.getMediaType();
        if (entities && request.hasEntity() && TEXT_TYPES.stream().anyMatch(mediaType::isCompatible)) {
            String body = read(request.getEntityStream());
            request.setEntityStream(new ByteArrayInputStream(body.getBytes(UTF_8))); // we just depleted the original stream
            return " " + mediaType + ":\n" + body;
        } else {
            return "";
        }
    }

    private String read(InputStream inputStream) {
        try (Scanner scanner = new Scanner(inputStream).useDelimiter("\\Z")) {
            return scanner.next();
        }
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
                uri = uriInfo.getBaseUri().relativize(uri);
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

    private static final List<MediaType> TEXT_TYPES = asList(
        APPLICATION_FORM_URLENCODED_TYPE,
        APPLICATION_JSON_TYPE,
        APPLICATION_XML_TYPE
    );
}
