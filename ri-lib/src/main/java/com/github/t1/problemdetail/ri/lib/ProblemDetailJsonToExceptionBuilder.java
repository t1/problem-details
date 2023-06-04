package com.github.t1.problemdetail.ri.lib;

import com.github.t1.problemdetail.Extension;
import com.github.t1.problemdetail.Instance;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.config.PropertyVisibilityStrategy;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
public class ProblemDetailJsonToExceptionBuilder extends Throwable {
    private static final Map<String, Class<? extends RuntimeException>> REGISTRY = new HashMap<>();

    static {
        register(NullPointerException.class);
        register(RuntimeException.class);
        register(NotFoundException.class);
    }

    public static String register(Class<? extends RuntimeException> exceptionType) {
        String typeUri = ProblemDetails.buildType(exceptionType).toString();
        REGISTRY.put(typeUri, exceptionType);
        return typeUri;
    }

    public ProblemDetailJsonToExceptionBuilder(InputStream entityStream) {
        this.body = Json.createReader(entityStream).readObject();
        String typeUri = body.getString("type", null);
        this.type = REGISTRY.getOrDefault(typeUri, null);
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

        String json = output.build().toString();
        throw JSONB.fromJson(json, type);
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
        @Override
        public boolean isVisible(Field field) {
            return Modifier.isPublic(field.getModifiers()) || isOpen(field.getDeclaringClass().getModule());
        }

        private boolean isOpen(Module module) {
            return module.getDescriptor() == null || module.getDescriptor().isOpen();
        }

        @Override
        public boolean isVisible(Method method) {
            return Modifier.isPublic(method.getModifiers());
        }
    };
    private static final Jsonb JSONB = JsonbBuilder.create(new JsonbConfig().withPropertyVisibilityStrategy(FIELD_ACCESS));
}
