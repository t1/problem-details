package com.github.t1.problemdetaildemoapp;

import lombok.extern.slf4j.Slf4j;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.Provider;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static jakarta.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED_TYPE;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static jakarta.ws.rs.core.MediaType.APPLICATION_XML_TYPE;
import static jakarta.ws.rs.core.MediaType.CHARSET_PARAMETER;

@Slf4j
@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {
    public static boolean LOG_REQUEST_ENTITY = false;

    @Override
    public void filter(ContainerRequestContext request) throws IOException {
        log.info("{} request {}{}", request.getMethod(), request.getUriInfo().getPath(), entity(request));
    }

    private String entity(ContainerRequestContext request) throws IOException {
        MediaType mediaType = request.getMediaType();
        if (LOG_REQUEST_ENTITY && log.isDebugEnabled() && request.hasEntity() && TEXT_TYPES.stream().anyMatch(mediaType::isCompatible)) {
            byte[] bytes = request.getEntityStream().readAllBytes();
            request.setEntityStream(new ByteArrayInputStream(bytes)); // we've just depleted the original stream
            String charset = mediaType.getParameters().getOrDefault(CHARSET_PARAMETER, UTF_8.name());
            return " " + mediaType + ":\n" + new String(bytes, charset);
        } else {
            return "";
        }
    }

    @Override public void filter(ContainerRequestContext request, ContainerResponseContext response) {
        log.info("{} response {} -> {}:{}{}", request.getMethod(), request.getUriInfo().getPath(),
            response.getStatusInfo(), response.getMediaType(), response.hasEntity() ? (":\n" + response.getEntity()) : "");
    }

    private static final List<MediaType> TEXT_TYPES = asList(
        APPLICATION_FORM_URLENCODED_TYPE,
        APPLICATION_JSON_TYPE,
        APPLICATION_XML_TYPE
    );
}
