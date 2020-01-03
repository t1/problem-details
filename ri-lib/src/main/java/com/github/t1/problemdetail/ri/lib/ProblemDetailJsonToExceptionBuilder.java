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
import java.util.stream.Stream;

import static com.github.t1.problemdetail.ri.lib.ProblemDetailExceptionRegistry.REGISTRY;

@Slf4j
public class ProblemDetailJsonToExceptionBuilder {
    public ProblemDetailJsonToExceptionBuilder(InputStream entityStream) {
        this.body = Json.createReader(entityStream).readObject();
        String typeUri = body.getString("type", null);
        this.type = REGISTRY.computeIfAbsent(typeUri, ProblemDetailExceptionRegistry::computeFrom);
    }

    private final JsonObject body;
    private final Class<? extends RuntimeException> type;

    private final JsonObjectBuilder output = Json.createObjectBuilder();

    /**
     * Throws an exception; if the type wasn't {@link ProblemDetailExceptionRegistry#register(Class) registered},
     * a {@link IllegalArgumentException} is thrown.
     */
    public void trigger() {
        throw build();
    }

    public RuntimeException build() {
        if (type == null)
            return new IllegalArgumentException("no registered exception found for `type` field in " + body);

        setInstance();
        setExtensions();

        JsonObject json = output.build();
        return (json.isEmpty())
            ? newInstance()
            : JSONB.fromJson(json.toString(), type);
    }

    @SneakyThrows(ReflectiveOperationException.class)
    private RuntimeException newInstance() {
        String detail = (body == null || !body.containsKey("detail") || body.isNull("detail")) ? null
            : body.getString("detail");
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
