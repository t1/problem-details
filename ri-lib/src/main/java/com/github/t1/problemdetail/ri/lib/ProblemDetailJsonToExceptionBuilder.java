package com.github.t1.problemdetail.ri.lib;

import com.github.t1.problemdetail.Extension;
import com.github.t1.problemdetail.Instance;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.config.PropertyVisibilityStrategy;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static com.github.t1.problemdetail.ri.lib.ProblemDetails.URN_PROBLEM_TYPE_PREFIX;
import static com.google.common.base.CaseFormat.LOWER_HYPHEN;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;

@Slf4j
public class ProblemDetailJsonToExceptionBuilder extends Throwable {
    private static final Map<String, Class<? extends RuntimeException>> REGISTRY = new HashMap<>();

    public static String register(Class<? extends RuntimeException> exceptionType) {
        String typeUri = ProblemDetails.buildTypeUri(exceptionType).toString();
        REGISTRY.put(typeUri, exceptionType);
        return typeUri;
    }

    private static Class<? extends RuntimeException> computeFrom(String type) {
        if (type != null && type.startsWith(URN_PROBLEM_TYPE_PREFIX)) {
            return computeFromUrn(type.substring(URN_PROBLEM_TYPE_PREFIX.length()));
        } else {
            return null;
        }
    }

    private static Class<? extends RuntimeException> computeFromUrn(String type) {
        String camel = LOWER_HYPHEN.to(UPPER_CAMEL, type);
        Class<? extends RuntimeException> cls = forName("java.lang." + camel + "Exception");
        if (cls != null)
            return cls;
        return forName("javax.ws.rs." + camel + "Exception");
    }

    private static Class<? extends RuntimeException> forName(String name) {
        try {
            Class<?> t = Class.forName(name);
            //noinspection unchecked
            return RuntimeException.class.isAssignableFrom(t)
                ? (Class<? extends RuntimeException>) t
                : null;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public ProblemDetailJsonToExceptionBuilder(InputStream entityStream) {
        this.body = Json.createReader(entityStream).readObject();
        String typeUri = body.getString("type", null);
        this.type = REGISTRY.computeIfAbsent(typeUri, ProblemDetailJsonToExceptionBuilder::computeFrom);
    }

    private final JsonObject body;
    private final Class<? extends RuntimeException> type;

    private final JsonObjectBuilder output = Json.createObjectBuilder();

    /**
     * Throws an exception; if the type wasn't {@link #register(Class) registered},
     * a {@link IllegalArgumentException} is thrown.
     */
    public void trigger() {
        if (type == null)
            throw new IllegalArgumentException("no registered exception found for `type` field in " + body);

        setInstance();
        setExtensions();

        JsonObject json = output.build();
        throw (json.isEmpty())
            ? newInstance()
            : JSONB.fromJson(json.toString(), type);
    }

    @SneakyThrows(ReflectiveOperationException.class)
    private RuntimeException newInstance() {
        String detail = (body == null || !body.containsKey("detail")) ? null : body.getString("detail");
        Constructor<? extends RuntimeException> messageConstructor = findMessageConstructor();
        if (detail == null || messageConstructor == null)
            return type.getConstructor().newInstance();
        return messageConstructor.newInstance(detail);
    }

    private Constructor<? extends RuntimeException> findMessageConstructor() {
        try {
            return type.getConstructor(String.class);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private void setInstance() {
        if (!body.containsKey("instance"))
            return;
        String value = body.getString("instance");
        Stream.of(type.getDeclaredFields())
            .filter(field -> field.isAnnotationPresent(Instance.class))
            .findAny().ifPresent(field -> output.add(field.getName(), value));
    }

    private void setExtensions() {
        Stream.of(type.getDeclaredFields())
            .filter(field -> field.isAnnotationPresent(Extension.class))
            .forEach(field -> {
                String annotatedName = field.getAnnotation(Extension.class).value();
                String name = annotatedName.isEmpty() ? field.getName() : annotatedName;
                if (body.containsKey(name)) {
                    output.add(field.getName(), body.getValue("/" + name));
                }
            });
    }

    private static final PropertyVisibilityStrategy FIELD_ACCESS = new PropertyVisibilityStrategy() {
        @Override public boolean isVisible(Field field) { return true; }

        @Override public boolean isVisible(Method method) { return false; }
    };
    private static final Jsonb JSONB = JsonbBuilder.create(new JsonbConfig().withPropertyVisibilityStrategy(FIELD_ACCESS));
}
