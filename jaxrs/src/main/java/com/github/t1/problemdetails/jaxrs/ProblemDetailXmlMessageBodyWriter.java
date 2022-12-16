package com.github.t1.problemdetails.jaxrs;

import com.github.t1.problemdetails.jaxrs.lib.ProblemXml;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;

import static org.eclipse.microprofile.problemdetails.Constants.PROBLEM_DETAIL_XML;

@Provider
public class ProblemDetailXmlMessageBodyWriter implements MessageBodyWriter<Map<String, Object>> {
    private static final MediaType PROBLEM_DETAIL_XML_TYPE = MediaType.valueOf(PROBLEM_DETAIL_XML);

    @Override public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return PROBLEM_DETAIL_XML_TYPE.isCompatible(mediaType);
    }

    @Override public void writeTo(Map<String, Object> map, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                                  MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException {
        new ProblemXml(map).writeTo(entityStream);
    }
}
