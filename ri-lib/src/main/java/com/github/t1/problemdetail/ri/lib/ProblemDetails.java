package com.github.t1.problemdetail.ri.lib;

import com.github.t1.problemdetail.Detail;
import com.github.t1.problemdetail.Instance;
import com.github.t1.problemdetail.LogLevel;
import com.github.t1.problemdetail.Logging;
import com.github.t1.problemdetail.Extension;
import com.github.t1.problemdetail.Status;
import com.github.t1.problemdetail.Title;
import com.github.t1.problemdetail.Type;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response.StatusType;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import static com.github.t1.problemdetail.LogLevel.AUTO;
import static java.util.stream.Collectors.joining;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.Family.CLIENT_ERROR;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

/**
 * Tech stack independent collector. Template methods can be overridden to provide tech stack specifics.
 */
public abstract class ProblemDetails {
    protected final Exception exception;
    protected final Class<? extends Exception> type;

    @Getter private final StatusType status;
    @Getter private final Object body;
    @Getter private final String mediaType;
    @Getter private final String logMessage;

    public ProblemDetails(Exception exception) {
        this.exception = exception;
        this.type = exception.getClass();
        this.status = buildStatus();
        this.body = buildBody();
        this.mediaType = buildResponseMediaType();
        this.logMessage = buildLogMessage();

        log(logMessage);
    }

    protected Object buildBody() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", buildType());

        body.put("title", buildTitle());

        body.put("status", status.getStatusCode());

        String detail = buildDetail();
        if (detail != null) {
            body.put("detail", detail);
        }

        body.put("instance", buildInstance());

        body.putAll(buildExtensions());

        return body;
    }

    protected StatusType buildStatus() {
        if (type.isAnnotationPresent(Status.class)) {
            return type.getAnnotation(Status.class).value();
        } else if (exception instanceof IllegalArgumentException) {
            return BAD_REQUEST;
        } else {
            return fallbackStatus();
        }
    }

    protected StatusType fallbackStatus() {
        return INTERNAL_SERVER_ERROR;
    }

    protected URI buildType() {
        return URI.create(type.isAnnotationPresent(Type.class)
            ? type.getAnnotation(Type.class).value()
            : "urn:problem-type:" + wordsFromTypeName('-').toLowerCase());
    }

    protected String buildTitle() {
        return type.isAnnotationPresent(Title.class)
            ? type.getAnnotation(Title.class).value()
            : wordsFromTypeName(' ');
    }

    protected String wordsFromTypeName(char delimiter) {
        String message = camelToWords(type.getSimpleName(), delimiter);
        if (message.endsWith(delimiter + "Exception"))
            message = message.substring(0, message.length() - 10);
        return message;
    }

    private String camelToWords(String input, char delimiter) {
        StringBuilder out = new StringBuilder();
        input.codePoints().forEach(c -> {
            if (Character.isUpperCase(c) && out.length() > 0) {
                out.append(delimiter);
            }
            out.appendCodePoint(c);
        });
        return out.toString();
    }

    protected String buildDetail() {
        List<Object> details = new ArrayList<>();
        for (Method method : type.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Detail.class)) {
                details.add(invoke(method));
            }
        }
        for (Field field : type.getDeclaredFields()) {
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
        String instance = null;
        for (Method method : type.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Instance.class)) {
                instance = invoke(method).toString();
            }
        }
        for (Field field : type.getDeclaredFields()) {
            if (field.isAnnotationPresent(Instance.class)) {
                instance = get(field).toString();
            }
        }
        if (instance == null)
            return URI.create("urn:uuid:" + UUID.randomUUID());
        return createSafeUri(instance);
    }

    private URI createSafeUri(String string) {
        try {
            return new URI(string);
        } catch (URISyntaxException e) {
            return UriBuilder.fromUri("urn:invalid-uri-syntax")
                .queryParam("source", string)
                .queryParam("exception", e)
                .build();
        }
    }

    protected Map<String, Object> buildExtensions() {
        Map<String, Object> extensions = new TreeMap<>();
        for (Method method : type.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Extension.class)) {
                extensions.put(extensionName(method), invoke(method));
            }
        }
        for (Field field : type.getDeclaredFields()) {
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

    protected String buildResponseMediaType() {
        String format = findMediaTypeSubtype();

        // browsers send, e.g., `text/html, application/xhtml+xml, application/xml;q=0.9, */*;q=0.8`
        // so the extra `problem+` is acceptable only by the wildcard and that starts a download
        return "xhtml+xml".equals(format) ? "text/html" : "application/problem+" + format;
    }

    protected abstract String findMediaTypeSubtype();

    private String buildLogMessage() {
        return "ProblemDetail:\n" + formatBody() + "\n"
            + "Exception";
    }

    private Object formatBody() {
        return (body instanceof Map)
            ? ((Map<?, ?>) body).entrySet().stream()
            .map(entry -> "  " + entry.getKey() + ": " + entry.getValue())
            .collect(joining("\n"))
            : String.valueOf(body);
    }


    private void log(String message) {
        Logger logger = buildLogger();
        switch (buildLogLevel()) {
            case AUTO:
                if (CLIENT_ERROR.equals(status.getFamily())) {
                    logger.debug(message);
                } else {
                    logger.error(message);
                }
                break;
            case ERROR:
                logger.error(message);
                break;
            case WARNING:
                logger.warn(message);
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
        return (logging == null || logging.to().isEmpty()) ? LoggerFactory.getLogger(type)
            : LoggerFactory.getLogger(logging.to());
    }

    private LogLevel buildLogLevel() {
        Logging logging = findLoggingAnnotation();
        return (logging == null) ? AUTO : logging.at();
    }

    private Logging findLoggingAnnotation() {
        Logging onType = type.getAnnotation(Logging.class);
        Logging onPackage = type.getPackage().getAnnotation(Logging.class);
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
