package com.github.t1.problemdetailmapper;

import com.github.t1.problemdetail.ProblemDetail;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.Provider;
import jakarta.xml.bind.JAXB;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import static com.github.t1.problemdetail.Constants.PROBLEM_DETAIL_XML_TYPE;
import static jakarta.ws.rs.core.MediaType.APPLICATION_XML_TYPE;

@Provider
public class ProblemDetailXmlMessageBodyReader implements MessageBodyReader<ProblemDetail> {
    @Override public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return ProblemDetail.class.isAssignableFrom(type)
            && (APPLICATION_XML_TYPE.isCompatible(mediaType) || PROBLEM_DETAIL_XML_TYPE.isCompatible(mediaType));
    }

    @Override public ProblemDetail readFrom(Class<ProblemDetail> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                                            MultivaluedMap<String, String> httpHeaders, InputStream entityStream) {
        return JAXB.unmarshal(entityStream, type);
    }
}
