package com.github.t1.problemdetailmapper;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;

import static jakarta.ws.rs.core.MediaType.TEXT_HTML_TYPE;

@Provider
public class ProblemDetailHtmlMessageBodyWriter implements MessageBodyWriter<Map<String, Object>> {
    @Override public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return Map.class.isAssignableFrom(type) && TEXT_HTML_TYPE.isCompatible(mediaType);
    }

    @Override public void writeTo(Map<String, Object> problem, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) {
        PrintWriter out = new PrintWriter(new OutputStreamWriter(entityStream));

        out.print("" +
            "<html>\n" +
            "<head>\n" +
            "    <style>\n" +
            "        body {\n" +
            "            font-family: \"Fira Code\", \"Courier New\", Courier, monospace;\n" +
            "            font-size: 14px;\n" +
            "        }\n" +
            "\n" +
            "        table {\n" +
            "            margin-top: 24pt;\n" +
            "            border: 1px solid rgb(221, 221, 221);\n" +
            "            border-collapse: collapse;\n" +
            "            box-sizing: border-box;\n" +
            "            color: rgb(51, 51, 51);\n" +
            "        }\n" +
            "\n" +
            "        tr {\n" +
            "            height: 37px;\n" +
            "        }\n" +
            "\n" +
            "        td {\n" +
            "            border: 1px solid rgb(221, 221, 221);\n" +
            "            border-collapse: collapse;\n" +
            "            padding: 8px 8px 0;\n" +
            "            vertical-align: top;\n" +
            "        }\n" +
            "    </style>\n" +
            "    <title>Problem Detail: " + problem.get("title") + "</title>\n" +
            "</head>\n" +
            "<body>\n" +
            "<h1>" + problem.get("title") + "</h1>\n" +
            "\n" +
            "<table>\n");
        problem.forEach((key, value) -> printColumn(out, key, value));
        out.print("" +
            "</table>\n" +
            "</body>\n" +
            "</html>\n");
        out.flush();
    }

    private void printColumn(PrintWriter out, String title, Object value) {
        if (value != null) {
            out.print("" +
                "    <tr>\n" +
                "        <td>" + title + "</td>\n" +
                "        <td>" + value + "</td>\n" +
                "    </tr>\n");
        }
    }
}
