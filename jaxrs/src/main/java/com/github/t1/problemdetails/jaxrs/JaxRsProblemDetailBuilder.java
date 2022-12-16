package com.github.t1.problemdetails.jaxrs;

import com.github.t1.problemdetails.jaxrs.lib.ProblemDetailBuilder;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.problemdetails.ResponseStatus;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.net.URI;

import static org.eclipse.microprofile.problemdetails.Constants.EXCEPTION_MESSAGE_AS_DETAIL;
import static org.eclipse.microprofile.problemdetails.Constants.EXCEPTION_MESSAGE_AS_DETAIL_DEFAULT;

class JaxRsProblemDetailBuilder extends ProblemDetailBuilder {
    private final HttpHeaders requestHeaders;
    private final Response response;

    JaxRsProblemDetailBuilder(Throwable exception, HttpHeaders requestHeaders) {
        this(exception, requestHeaders, null);
    }

    JaxRsProblemDetailBuilder(Throwable exception, HttpHeaders requestHeaders, Response response) {
        super(exception);
        this.requestHeaders = requestHeaders;
        this.response = response;
    }

    @Override protected ResponseStatus buildStatus() {
        return (response != null && ResponseStatus.allowed(response.getStatus()))
            ? ResponseStatus.valueOf(response.getStatus())
            : super.buildStatus();
    }

    @Override protected URI buildTypeUri() {
        if (response != null)
            return problemTypeUrn(response.getStatusInfo().getReasonPhrase());
        return super.buildTypeUri();
    }

    @Override protected String fallbackTitle() {
        if (response != null)
            return response.getStatusInfo().getReasonPhrase();
        return super.fallbackTitle();
    }

    @Override protected boolean useExceptionMessageAsDetail() {
        return exceptionMessageAsDetail() && !isDefaultStatusMessage();
    }

    private boolean exceptionMessageAsDetail() {
        return ConfigProvider.getConfig()
            .getOptionalValue(EXCEPTION_MESSAGE_AS_DETAIL, Boolean.class)
            .orElse(EXCEPTION_MESSAGE_AS_DETAIL_DEFAULT);
    }

    /** We don't want to repeat JAX-RS default messages like `400 Bad Request` */
    private boolean isDefaultStatusMessage() {
        return defaultStatusMessage().equals(exception.getMessage());
    }

    private String defaultStatusMessage() {
        int statusCode = getStatus().code;
        return "HTTP " + statusCode + " " + Status.fromStatusCode(statusCode);
    }

    @Override protected String findMediaTypeSubtype() {
        for (MediaType accept : requestHeaders.getAcceptableMediaTypes()) {
            if ("application".equals(accept.getType())) {
                return accept.getSubtype();
            }
        }
        return "json";
    }

    @Override public JaxRsProblemDetailBuilder log() {
        super.log();
        return this;
    }

    public Response toResponse() {
        return Response
            .status(getStatus().code)
            .entity(getBody())
            .header("Content-Type", getMediaType())
            .build();
    }
}
