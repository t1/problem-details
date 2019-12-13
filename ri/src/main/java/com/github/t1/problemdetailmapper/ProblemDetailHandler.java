package com.github.t1.problemdetailmapper;

import com.github.t1.problemdetail.Extension;
import com.github.t1.problemdetail.Instance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.config.PropertyVisibilityStrategy;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.ext.Provider;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static com.github.t1.problemdetail.Constants.PROBLEM_DETAIL_JSON_TYPE;

@Slf4j
@Provider
public class ProblemDetailHandler implements ClientResponseFilter {
    public static final Map<String, Class<? extends RuntimeException>> CONFIG = new HashMap<>();

    static {
        CONFIG.put("urn:problem-type:null-pointer", NullPointerException.class);
        CONFIG.put("urn:problem-type:runtime", RuntimeException.class);
        CONFIG.put("urn:problem-type:not-found", NotFoundException.class);
    }

    @Override public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) {
        // TODO XML
        if (PROBLEM_DETAIL_JSON_TYPE.isCompatible(responseContext.getMediaType())) {
            JsonObject detail = Json.createReader(responseContext.getEntityStream()).readObject();
            String type = detail.getString("type", null);
            if (CONFIG.containsKey(type)) {
                throw new ExceptionBuilder(detail, CONFIG.get(type)).build();
            } else {
                log.debug("no exception found to map from problem detail type [" + type + "]");
            }
        }
    }

    @RequiredArgsConstructor
    private static class ExceptionBuilder extends Throwable {
        private final JsonObject input;
        private final Class<? extends RuntimeException> type;

        private final JsonObjectBuilder output = Json.createObjectBuilder();

        RuntimeException build() {
            setInstance();
            setExtensions();

            String json = output.build().toString();
            return JSONB.fromJson(json, type);
        }

        private void setInstance() {
            if (!input.containsKey("instance"))
                return;
            String value = input.getString("instance");
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
                    if (input.containsKey(name)) {
                        output.add(field.getName(), input.getValue("/" + name));
                    }
                });
        }

        private static final PropertyVisibilityStrategy FIELD_ACCESS = new PropertyVisibilityStrategy() {
            @Override public boolean isVisible(Field field) { return true; }

            @Override public boolean isVisible(Method method) { return false; }
        };
        private static final Jsonb JSONB = JsonbBuilder.create(new JsonbConfig().withPropertyVisibilityStrategy(FIELD_ACCESS));
    }
}
