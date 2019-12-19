package com.github.t1.problemdetail.ri;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;

@Provider
public class YamlMessageBodyWriter implements MessageBodyWriter<Object> {
    public static final String APPLICATION_YAML = "application/yaml";
    public static final MediaType APPLICATION_YAML_TYPE = MediaType.valueOf(APPLICATION_YAML);

    @Override public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return APPLICATION_YAML_TYPE.isCompatible(mediaType) ||
            (mediaType.getType().equals("application") && mediaType.getSubtype().endsWith("+yaml"));
    }

    @Override public void writeTo(Object o, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                                  MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) {
        Yaml yaml = new Yaml(new ConfigurableRepresenter().asString(URI.class, new Tag("!uri")), new DumperOptions());
        yaml.dump(o, new OutputStreamWriter(entityStream));
    }

    static class ConfigurableRepresenter extends Representer {
        public ConfigurableRepresenter asString(Class<?> type, Tag tag) {
            representers.put(type, new RepresentStr());
            addClassTag(type, tag);
            return this;
        }

        class RepresentStr extends RepresentString {}
    }
}
