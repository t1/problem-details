package com.github.t1.problemdetails.jaxrs.lib;

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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
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

            String packageName = exceptionType.getPackage().getName();
            if (packageName.startsWith("java.") || packageName.startsWith("javax.")) {
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
        String details = Property.allIn(exception)
            .filter(property -> property.isAnnotationPresent(Detail.class))
            .map(Property::get)
            .filter(Objects::nonNull)
            .map(Object::toString)
            .collect(joining(" "));
        return (details.isEmpty()) ? null : details;
    }

    protected URI buildInstance() {
        boolean anyAnnotatedInstance = false;
        for (Property property : Property.allIn(exception).collect(toList())) {
            if (property.isAnnotationPresent(Instance.class)) {
                anyAnnotatedInstance = true;
                Object o = property.get();
                if (o != null) {
                    return createSafeUri(o.toString());
                }
            }
        }
        return (anyAnnotatedInstance) ? null : createSafeUri("urn:uuid:" + UUID.randomUUID());
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
        return Property.allIn(exception)
            .filter(property -> property.isAnnotationPresent(Extension.class))
            .filter(Property::notNull)
            .collect(toMap(this::extensionName, Property::get, (a, b) -> a, TreeMap::new));
    }

    private String extensionName(Property property) {
        String annotatedName = property.getAnnotation(Extension.class).value();
        return annotatedName.isEmpty() ? property.getName() : annotatedName;
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
            logMessage = "ProblemDetails:" + formatBody(getBody());
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
            case WARN:
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
