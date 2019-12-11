package com.github.t1.problemdetaildemoapp;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static org.springframework.http.MediaType.TEXT_PLAIN;

@RestController
@RequestMapping(path = "/standard")
public class StandardExceptionBoundary {
    @PostMapping(path = "/plain-bad-request")
    public void plainBadRequest() {
        //noinspection ConstantConditions
        throw HttpClientErrorException.create(BAD_REQUEST, null, null, null, null);
    }

    @PostMapping(path = "/bad-request-with-message")
    public void badRequestWithMessage() {
        //noinspection ConstantConditions
        throw HttpClientErrorException.create("some message", BAD_REQUEST, null, null, null, null);
    }

    @PostMapping(path = "/bad-request-with-response")
    public void badRequestWithResponse() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(TEXT_PLAIN);
        //noinspection ConstantConditions
        throw new HttpClientErrorException(BAD_REQUEST, null, headers, "the body".getBytes(UTF_8), UTF_8);
    }

    @PostMapping(path = "/plain-service-unavailable")
    public void plainServiceUnavailable() {
        //noinspection ConstantConditions
        throw HttpServerErrorException.create(SERVICE_UNAVAILABLE, null, null, null, null);
    }

    @PostMapping(path = "/illegal-argument-without-message")
    public void illegalArgumentWithoutMessage() {
        throw new IllegalArgumentException();
    }

    @PostMapping(path = "/illegal-argument-with-message")
    public void illegalArgumentWithMessage() {
        throw new IllegalArgumentException("some message");
    }

    @PostMapping(path = "/npe-without-message")
    public void npeWithoutMessage() {
        throw new NullPointerException();
    }

    @PostMapping(path = "/npe-with-message")
    public void npeWithMessage() {
        throw new NullPointerException("some message");
    }
}
