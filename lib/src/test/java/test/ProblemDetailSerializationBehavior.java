package test;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import test.TestData.SomeException;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import javax.xml.bind.JAXB;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;

import static org.assertj.core.api.BDDAssertions.then;
import static test.TestData.PROBLEM_DETAILS;
import static test.TestData.v2;
import static test.TestData.v3;
import static test.TestData.v4;

class ProblemDetailSerializationBehavior {
    @Test void shouldSerializeAsJson() {
        Jsonb jsonb = JsonbBuilder.create(new JsonbConfig().withFormatting(true));

        String json = jsonb.toJson(PROBLEM_DETAILS.getBody());

        then(json).isEqualTo("\n" + PROBLEM_DETAIL_JSON);
    }

    // TODO support XML https://github.com/t1/problem-details/issues/6
    @Disabled @Test void shouldSerializeAsXml() {
        StringWriter xml = new StringWriter();

        JAXB.marshal(PROBLEM_DETAILS.getBody(), xml);

        then(xml.toString()).isEqualTo(PROBLEM_DETAIL_XML);
    }

    @Test void shouldDeserializeJson() {
        SomeException someException = JsonbBuilder.create().fromJson(PROBLEM_DETAIL_JSON, SomeException.class);

        thenIsExpected(someException);
    }

    @Test void shouldDeserializeXml() {
        SomeException someException = JAXB.unmarshal(new StringReader(PROBLEM_DETAIL_XML), SomeException.class);

        thenIsExpected(someException);
    }

    static void thenIsExpected(SomeException someException) {
        then(someException.detail).isEqualTo("some-detail");
        then(someException.instance).isEqualTo(URI.create("urn:some-instance"));
        then(someException.k1).isEqualTo("v1");
        then(someException.k2).isEqualTo(v2());
        then(someException.k3).isEqualTo(v3());
        then(someException.k4).isEqualTo(v4());
        then(someException.k5).isEqualTo(null);
    }

    static final String PROBLEM_DETAIL_JSON = "{\n" +
        "    \"type\": \"urn:some-type\",\n" +
        "    \"title\": \"some-title\",\n" +
        "    \"status\": 400,\n" +
        "    \"detail\": \"some-detail\",\n" +
        "    \"instance\": \"urn:some-instance\",\n" +
        "    \"k1\": \"v1\",\n" +
        "    \"k2\": [\n" +
        "        \"urn:1\",\n" +
        "        null,\n" +
        "        \"urn:2\"\n" +
        "    ],\n" +
        "    \"k3\": {\n" +
        "        \"k3.1\": \"v3.1\",\n" +
        "        \"k3.2\": \"v3.2\"\n" +
        "    },\n" +
        "    \"k4\": {\n" +
        "        \"k4.1\": [\n" +
        "            \"v4.1.1\",\n" +
        "            \"v4.1.2\",\n" +
        "            \"v4.1.3\"\n" +
        "        ],\n" +
        "        \"k4.2\": [\n" +
        "            \"v4.2.1\"\n" +
        "        ],\n" +
        "        \"k4.3\": [\n" +
        "            \"v4.3.1\",\n" +
        "            \"v4.3.2\"\n" +
        "        ],\n" +
        "        \"k4.4\": [\n" +
        "        ]\n" +
        "    }\n" +
        "}";

    static final String PROBLEM_DETAIL_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
        "<problem xmlns=\"urn:ietf:rfc:7807\">\n" +
        "    <type>urn:some-type</type>\n" +
        "    <title>some-title</title>\n" +
        "    <status>400</status>\n" +
        "    <detail>some-detail</detail>\n" +
        "    <instance>urn:some-instance</instance>\n" +
        "    <k1>v1</k1>\n" +
        "    <k2>\n" +
        "        <i>urn:1</i>\n" +
        "        <i/>\n" +
        "        <i>urn:2</i>\n" +
        "    </k2>\n" +
        "    <k3>\n" +
        "        <k3.1>v3.1</k3.1>\n" +
        "        <k3.2>v3.2</k3.2>\n" +
        "    </k3>\n" +
        "    <k4>\n" +
        "        <k4.1>\n" +
        "            <i>v4.1.1</i>\n" +
        "            <i>v4.1.2</i>\n" +
        "            <i>v4.1.3</i>\n" +
        "        </k4.1>\n" +
        "        <k4.2>\n" +
        "            <i>v4.2.1</i>\n" +
        "        </k4.2>\n" +
        "        <k4.3>\n" +
        "            <i>v4.3.1</i>\n" +
        "            <i>v4.3.2</i>\n" +
        "        </k4.3>\n" +
        "        <k4.4/>\n" +
        "    </k4>\n" +
        "    <k5/>\n" +
        "</problem>\n";
}
