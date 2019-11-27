package com.github.t1.problemdetail.ri.lib;

import lombok.SneakyThrows;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

import static javax.xml.transform.OutputKeys.ENCODING;
import static javax.xml.transform.OutputKeys.INDENT;
import static javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION;

public class ProblemXml {
    public static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();

    private final Document document;
    private Element current;

    @SneakyThrows(ParserConfigurationException.class)
    public ProblemXml(Object object) {
        document = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder().newDocument();
        current = document.createElementNS("urn:ietf:rfc:7807", "problem");
        document.appendChild(current);

        append(object);
    }

    @SuppressWarnings("unchecked") private void append(Object object) {
        if (object instanceof Map) {
            append((Map<String, Object>) object);
        } else if (object instanceof Iterable) {
            append((Iterable<Object>) object);
        } else if (object != null) {
            append(object.toString());
        }
    }

    private void append(Map<String, Object> map) {
        Element container = current;
        map.forEach((key, value) -> {
            current = document.createElement(key);
            append(value);
            container.appendChild(current);
        });
        current = container;
    }

    private void append(Iterable<Object> iterable) {
        Element container = current;
        iterable.forEach(item -> {
            current = document.createElement("i");
            append(item);
            container.appendChild(current);
        });
        current = container;
    }

    private void append(String string) {
        current.appendChild(document.createTextNode(string));
    }


    public void writeTo(OutputStream outputStream) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(outputStream);
        writeTo(writer);
    }

    @SneakyThrows(TransformerException.class)
    public void writeTo(Writer writer) throws IOException {
        document.normalize();
        // we write the `<?xml ...` declaration manually, as the OpenJDK serializer doesn't append a nl!
        // see https://bugs.openjdk.java.net/browse/JDK-7150637
        writer.append("<?xml version=\"").append(document.getXmlVersion()).append("\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
        Transformer transformer = transformer();
        transformer.setOutputProperty(ENCODING, "UTF-8");
        transformer.setOutputProperty(OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(INDENT, "yes");
        transformer.transform(new DOMSource(document), new StreamResult(writer));
    }

    private Transformer transformer() throws TransformerConfigurationException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setAttribute("indent-number", 4);
        return transformerFactory.newTransformer();
    }
}
