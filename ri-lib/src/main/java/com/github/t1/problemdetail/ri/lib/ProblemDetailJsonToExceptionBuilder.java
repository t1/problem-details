package com.github.t1.problemdetail.ri.lib;

import lombok.Getter;
import lombok.SneakyThrows;
import org.eclipse.microprofile.problemdetails.Extension;
import org.eclipse.microprofile.problemdetails.Instance;

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
import java.util.stream.Stream;

import static com.github.t1.problemdetail.ri.lib.ProblemDetailExceptionRegistry.REGISTRY;

// TODO also support XML problem detail bodies https://github.com/t1/problem-details/issues/6
public class ProblemDetailJsonToExceptionBuilder {
    protected final JsonObject body;
    private final @Getter(lazy = true) Class<? extends Throwable> type = initType();

    private final JsonObjectBuilder output = Json.createObjectBuilder();

    public ProblemDetailJsonToExceptionBuilder(InputStream entityStream) {
        this.body = Json.createReader(entityStream).readObject();
    }

    private Class<? extends Throwable> initType() {
        String typeUri = getTypeUri();
        return REGISTRY.computeIfAbsent(typeUri, ProblemDetailExceptionRegistry::computeFrom);
    }

    protected String getTypeUri() {
        return body.getString("type", null);
    }

    protected String getDetail() {
        return (body == null || !body.containsKey("detail") || body.isNull("detail")) ? null
            : body.getString("detail");
    }

    protected int getStatusCode() {
        return body.getInt("status");
    }

    /**
     * Throws an exception; if the type wasn't {@link ProblemDetailExceptionRegistry#register(Class) registered},
     * a {@link IllegalArgumentException} is thrown.
     */
    @SneakyThrows
    public void trigger() {
        throw build();
    }

    public Throwable build() {
        if (getType() == null)
            return new IllegalArgumentException("no registered exception found for `type` field in " + body);

        setInstance();
        setExtensions();

        JsonObject json = output.build();
        return (json.isEmpty())
            ? newInstance(getDetail(), getType())
            : JSONB.fromJson(json.toString(), getType());
    }

    @SneakyThrows(ReflectiveOperationException.class)
    protected static Throwable newInstance(String detail, Class<? extends Throwable> type) {
        if (detail == null)
            return type.getConstructor().newInstance();
        Constructor<? extends Throwable> constructorWithMessageParam = constructorWithMessageParam(type);
        if (constructorWithMessageParam == null)
            return type.getConstructor().newInstance();
        return constructorWithMessageParam.newInstance(detail);
    }

    private static Constructor<? extends Throwable> constructorWithMessageParam(Class<? extends Throwable> type) {
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
        Stream.of(getType().getDeclaredFields())
            .filter(field -> field.isAnnotationPresent(Instance.class))
            .findAny().ifPresent(field -> output.add(field.getName(), value));
    }

    private void setExtensions() {
        Stream.of(getType().getDeclaredFields())
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
