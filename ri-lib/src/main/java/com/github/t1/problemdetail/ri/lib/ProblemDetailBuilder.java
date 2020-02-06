package com.github.t1.problemdetail.ri.lib;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.problemdetails.Detail;
import org.eclipse.microprofile.problemdetails.Extension;
import org.eclipse.microprofile.problemdetails.Instance;
import org.eclipse.microprofile.problemdetails.LogLevel;
import org.eclipse.microprofile.problemdetails.Logging;
import org.eclipse.microprofile.problemdetails.ResponseStatus;
import org.eclipse.microprofile.problemdetails.Status;
import org.eclipse.microprofile.problemdetails.Title;
import org.eclipse.microprofile.problemdetails.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriBuilder;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;

import static java.util.stream.Collectors.joining;
import static org.eclipse.microprofile.problemdetails.LogLevel.AUTO;
import static org.eclipse.microprofile.problemdetails.ResponseStatus.BAD_REQUEST;
import static org.eclipse.microprofile.problemdetails.ResponseStatus.INTERNAL_SERVER_ERROR;

/**
 * Tech stack independent collector. Template methods can be overridden to provide tech stack specifics.
 */
@RequiredArgsConstructor
public abstract class ProblemDetailBuilder {
    public static final String URN_PROBLEM_TYPE_PREFIX = "urn:problem-type:";

    protected final @NonNull Throwable exception;

    private ResponseStatus status;
    private String mediaType;
    private Object body;
    private String logMessage;

    public Object getBody() {
        if (body == null) {
            body = buildBody();
        }
        return body;
    }

    protected Object buildBody() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", buildTypeUri());

        body.put("title", buildTitle());

        body.put("status", getStatus().code);

        String detail = buildDetail();
        if (detail != null) {
            body.put("detail", detail);
        }

        body.put("instance", buildInstance());

        body.putAll(buildExtensions());

        return body;
    }

    public ResponseStatus getStatus() {
        if (status == null) {
            status = buildStatus();
        }
        return status;
    }

    protected ResponseStatus buildStatus() {
        for (Throwable e = exception; e != null; e = e.getCause()) {
            Class<? extends @NonNull Throwable> exceptionType = exception.getClass();
            if (exceptionType.isAnnotationPresent(Status.class)) {
                return exceptionType.getAnnotation(Status.class).value();
            }

            if ("java.lang".equals(exceptionType.getPackage().getName())) {
                return INTERNAL_SERVER_ERROR;
            }
        }

        return BAD_REQUEST;
    }

    protected URI buildTypeUri() {
        return buildTypeUri(exception.getClass());
    }

    public static URI buildTypeUri(Class<? extends Throwable> type) {
        return type.isAnnotationPresent(Type.class)
            ? URI.create(type.getAnnotation(Type.class).value())
            : problemTypeUrn(wordsFromTypeName(type));
    }

    public static URI problemTypeUrn(String string) {
        return URI.create(URN_PROBLEM_TYPE_PREFIX + string.replace(' ', '-').toLowerCase());
    }

    protected String buildTitle() {
        return exception.getClass().isAnnotationPresent(Title.class)
            ? exception.getClass().getAnnotation(Title.class).value()
            : fallbackTitle();
    }

    protected String fallbackTitle() {
        return wordsFromTypeName(exception.getClass());
    }

    private static String wordsFromTypeName(Class<? extends Throwable> type) {
        String message = camelToWords(type.getSimpleName());
        if (message.endsWith(" Exception"))
            message = message.substring(0, message.length() - 10);
        return message;
    }

    private static String camelToWords(String input) {
        return String.join(" ", input.split("(?=\\p{javaUpperCase})"));
    }

    protected String buildDetail() {
        List<Object> details = new ArrayList<>();
        for (Method method : exception.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Detail.class)) {
                details.add(invoke(method));
            }
        }
        for (Field field : exception.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Detail.class)) {
                details.add(get(field));
            }
        }
        return (details.isEmpty())
            ? hasDefaultMessage() ? null : exception.getMessage()
            : details.stream().map(Object::toString).collect(joining(". "));
    }

    /** We don't want to repeat default messages like `400 Bad Request` */
    protected abstract boolean hasDefaultMessage();

    private Object invoke(Method method) {
        try {
            if (method.getParameterCount() != 0)
                return invocationFailed(method, "expected no args but got " + method.getParameterCount());
            method.setAccessible(true);
            return method.invoke(exception);
        } catch (IllegalAccessException e) {
            return invocationFailed(method, e);
        } catch (InvocationTargetException e) {
            return invocationFailed(method, e.getTargetException());
        }
    }

    private String invocationFailed(Method method, Object detail) {
        return "could not invoke " + method.getDeclaringClass().getSimpleName()
            + "." + method.getName() + ": " + detail;
    }

    private Object get(Field field) {
        try {
            field.setAccessible(true);
            return field.get(exception);
        } catch (IllegalAccessException e) {
            return "could not get " + field;
        }
    }

    protected URI buildInstance() {
        String instance = Arrays.stream(exception.getClass().getDeclaredFields())
            .filter(field -> field.isAnnotationPresent(Instance.class))
            .map(this::get)
            .filter(Objects::nonNull)
            .findAny()
            .map(Object::toString)
            .orElseGet(
                () -> Arrays.stream(exception.getClass().getDeclaredMethods())
                    .filter(method -> method.isAnnotationPresent(Instance.class))
                    .map(this::invoke)
                    .filter(Objects::nonNull)
                    .findAny()
                    .map(Object::toString)
                    .orElseGet(
                        () -> "urn:uuid:" + UUID.randomUUID()));
        return createSafeUri(instance);
    }

    private URI createSafeUri(String string) {
        try {
            return new URI(string);
        } catch (URISyntaxException e) {
            try {
                return new URI("urn:" + string.replace(' ', '+'));
            } catch (URISyntaxException ee) {
                return UriBuilder.fromUri("urn:invalid-uri-syntax")
                    .queryParam("source", string)
                    .queryParam("exception", e)
                    .build();
            }
        }
    }

    protected Map<String, Object> buildExtensions() {
        Map<String, Object> extensions = new TreeMap<>();
        for (Method method : exception.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Extension.class)) {
                extensions.put(extensionName(method), invoke(method));
            }
        }
        for (Field field : exception.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Extension.class)) {
                extensions.put(extensionName(field), get(field));
            }
        }
        return extensions;
    }

    private String extensionName(Member member) {
        String annotatedName = ((AnnotatedElement) member).getAnnotation(Extension.class).value();
        return annotatedName.isEmpty() ? member.getName() : annotatedName;
    }

    public String getMediaType() {
        if (mediaType == null) {
            mediaType = buildMediaType();
        }
        return mediaType;
    }

    protected String buildMediaType() {
        String format = findMediaTypeSubtype();

        // browsers send, e.g., `text/html, application/xhtml+xml, application/xml;q=0.9, */*;q=0.8`
        // so the extra `problem+` is acceptable only by the wildcard and that starts a download
        return "xhtml+xml".equals(format) ? "text/html" : "application/problem+" + format;
    }

    protected abstract String findMediaTypeSubtype();

    public String getLogMessage() {
        if (logMessage == null) {
            logMessage = "ProblemDetail:" + formatBody(getBody());
        }
        return logMessage;
    }

    private Object formatBody(Object body) {
        if (body == null) {
            return " - ";
        } else if (body instanceof Map) {
            return ((Map<?, ?>) body).entrySet().stream()
                .map(entry -> "  " + entry.getKey() + ": " + entry.getValue())
                .collect(joining("\n", "\n", "\n"));
        } else {
            return "\n" + body + "\n";
        }
    }


    public ProblemDetailBuilder log() {
        log(getLogMessage());
        return this;
    }

    private void log(String message) {
        Logger logger = buildLogger();
        switch (buildLogLevel()) {
            case AUTO:
                if (getStatus().code < 500) {
                    logger.info(message);
                } else {
                    logger.error(message, exception);
                }
                break;
            case ERROR:
                logger.error(message, exception);
                break;
            case WARNING:
                logger.warn(message, exception);
                break;
            case INFO:
                logger.info(message);
                break;
            case DEBUG:
                logger.debug(message);
                break;
            case OFF:
                break;
        }
    }

    private Logger buildLogger() {
        Logging logging = findLoggingAnnotation();
        return (logging == null || logging.to().isEmpty()) ? LoggerFactory.getLogger(exception.getClass())
            : LoggerFactory.getLogger(logging.to());
    }

    private LogLevel buildLogLevel() {
        Logging logging = findLoggingAnnotation();
        return (logging == null) ? AUTO : logging.at();
    }

    private Logging findLoggingAnnotation() {
        Logging onType = exception.getClass().getAnnotation(Logging.class);
        Logging onPackage = exception.getClass().getPackage().getAnnotation(Logging.class);
        if (onPackage == null)
            return onType;
        if (onType == null)
            return onPackage;
        return new Logging() {
            @Override public Class<? extends Annotation> annotationType() {
                throw new UnsupportedOperationException();
            }

            @Override public String to() {
                return (onType.to().isEmpty()) ? onPackage.to() : onType.to();
            }

            @Override public LogLevel at() {
                return (onType.at() == AUTO) ? onPackage.at() : onType.at();
            }
        };
    }
}
