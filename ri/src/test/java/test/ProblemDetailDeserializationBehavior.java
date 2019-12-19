package test;

import com.github.t1.problemdetail.ri.lib.ProblemDetail;
import com.github.t1.problemdetail.ri.ProblemDetailJsonMessageBodyReader;
import com.github.t1.problemdetail.ri.ProblemDetailXmlMessageBodyReader;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import javax.ws.rs.ext.MessageBodyReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.BDDAssertions.then;

public class ProblemDetailDeserializationBehavior {
    @Test void shouldDeserializeJson() {
        ProblemDetailJsonMessageBodyReader reader = new ProblemDetailJsonMessageBodyReader();

        ProblemDetail problemDetail = read(reader, "{" +
            "    \"type\": \"urn:problem-type:java.lang.NullPointerException\",\n" +
            "    \"title\": \"Null Pointer\",\n" +
            "    \"status\": 500,\n" +
            "    \"detail\": \"some message\",\n" +
            "    \"instance\": \"urn:uuid:d294b32b-9dda-4292-b51f-35f65b4bf64d\"\n" +
            "}");

        thenIsExpected(problemDetail);
    }

    @Test void shouldDeserializeXml() {
        ProblemDetailXmlMessageBodyReader reader = new ProblemDetailXmlMessageBodyReader();

        ProblemDetail problemDetail = read(reader, "" +
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<problem xmlns=\"urn:ietf:rfc:7807\">\n" +
            "    <type>urn:problem-type:java.lang.NullPointerException</type>\n" +
            "    <title>Null Pointer</title>\n" +
            "    <status>500</status>\n" +
            "    <detail>some message</detail>\n" +
            "    <instance>urn:uuid:d294b32b-9dda-4292-b51f-35f65b4bf64d</instance>\n" +
            "</problem>");

        thenIsExpected(problemDetail);
    }

    @SneakyThrows(IOException.class)
    private ProblemDetail read(MessageBodyReader<ProblemDetail> reader, String text) {
        return reader.readFrom(ProblemDetail.class, null, null, null, null, new ByteArrayInputStream(text.getBytes(UTF_8)));
    }

    private void thenIsExpected(ProblemDetail problemDetail) {
        then(problemDetail.getType()).isEqualTo(URI.create("urn:problem-type:java.lang.NullPointerException"));
        then(problemDetail.getTitle()).isEqualTo("Null Pointer");
        then(problemDetail.getStatus()).isEqualTo(500);
        then(problemDetail.getDetail()).isEqualTo("some message");
        then(problemDetail.getInstance()).isEqualTo(URI.create("urn:uuid:d294b32b-9dda-4292-b51f-35f65b4bf64d"));
    }
}
