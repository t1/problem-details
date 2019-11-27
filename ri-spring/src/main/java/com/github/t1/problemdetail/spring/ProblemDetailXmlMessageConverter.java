package com.github.t1.problemdetail.spring;

import com.github.t1.problemdetail.ri.lib.ProblemXml;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractGenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;

import static com.github.t1.problemdetail.Constants.PROBLEM_DETAIL_XML;

@Component
public class ProblemDetailXmlMessageConverter extends AbstractGenericHttpMessageConverter<LinkedHashMap<String, Object>> {
    public ProblemDetailXmlMessageConverter() { super(MediaType.valueOf(PROBLEM_DETAIL_XML)); }

    @Override protected LinkedHashMap<String, Object> readInternal(Class<? extends LinkedHashMap<String, Object>> clazz, HttpInputMessage inputMessage) {
        throw new UnsupportedOperationException();
    }

    @Override public LinkedHashMap<String, Object> read(Type type, @Nullable Class<?> contextClass, HttpInputMessage inputMessage) {
        throw new UnsupportedOperationException();
    }

    @Override protected void writeInternal(LinkedHashMap<String, Object> map, @Nullable Type type, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        new ProblemXml(map).writeTo(outputMessage.getBody());
    }
}
