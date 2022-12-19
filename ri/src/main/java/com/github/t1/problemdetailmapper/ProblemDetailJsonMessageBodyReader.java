package com.github.t1.problemdetailmapper;

import com.github.t1.problemdetail.ProblemDetail;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.Provider;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import static com.github.t1.problemdetail.Constants.PROBLEM_DETAIL_JSON_TYPE;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

@Provider
public class ProblemDetailJsonMessageBodyReader implements MessageBodyReader<ProblemDetail> {
    private static final Jsonb JSONB = JsonbBuilder.create();

    @Override public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type.equals(ProblemDetail.class)
               && (APPLICATION_JSON_TYPE.isCompatible(mediaType) || PROBLEM_DETAIL_JSON_TYPE.isCompatible(mediaType));
    }

    @Override public ProblemDetail readFrom(Class<ProblemDetail> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) {
        return JSONB.fromJson(entityStream, type);
    }
}
