package test;

import com.github.t1.problemdetailmapper.ProblemDetailHtmlMessageBodyWriter;
import com.github.t1.problemdetailmapper.ProblemDetailXmlMessageBodyWriter;
import com.github.t1.problemdetailmapper.YamlMessageBodyWriter;
import org.junit.jupiter.api.Test;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.BDDAssertions.then;

class ProblemDetailSerializationBehavior {
    public static final Map<String, Object> SOME_PROBLEM_DETAIL = new LinkedHashMap<>();

    static {
        SOME_PROBLEM_DETAIL.put("type", URI.create("urn:some-type"));
        SOME_PROBLEM_DETAIL.put("title", "some-title");
        SOME_PROBLEM_DETAIL.put("status", 400);
        SOME_PROBLEM_DETAIL.put("detail", "some-detail");
        SOME_PROBLEM_DETAIL.put("instance", URI.create("urn:some-instance"));
        SOME_PROBLEM_DETAIL.put("k1", "v1");
        SOME_PROBLEM_DETAIL.put("k2", asList(URI.create("urn:1"), null, URI.create("urn:2")));
        SOME_PROBLEM_DETAIL.put("k3", v3());
        SOME_PROBLEM_DETAIL.put("k4", v4());
        SOME_PROBLEM_DETAIL.put("k5", null);
    }

    private static Map<String, Object> v3() {
        Map<String, Object> v3 = new LinkedHashMap<>();
        v3.put("k3.1", "v3.1");
        v3.put("k3.2", "v3.2");
        return v3;
    }

    private static Map<String, Object> v4() {
        Map<String, Object> v4 = new LinkedHashMap<>();
        v4.put("k4.1", asList("v4.1.1", "v4.1.2", "v4.1.3"));
        v4.put("k4.2", singletonList("v4.2.1"));
        v4.put("k4.3", asList("v4.3.1", "v4.3.2"));
        v4.put("k4.4", emptyList());
        return v4;
    }

    @Test void shouldSerializeAsJson() {
        String out = JSONB.toJson(SOME_PROBLEM_DETAIL);

        then(out).isEqualTo("\n" +
            "{\n" +
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
            "}");
    }

    @Test void shouldSerializeAsXml() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // JAXB doesn't know out-of-the-box how to marshal a map; our writer does:
        new ProblemDetailXmlMessageBodyWriter().writeTo(SOME_PROBLEM_DETAIL, null, null, null, null, null, out);

        then(out.toString()).isEqualTo(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
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
                "</problem>\n");
    }

    @Test void shouldSerializeAsHtml() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        new ProblemDetailHtmlMessageBodyWriter().writeTo(SOME_PROBLEM_DETAIL, null, null, null, null, null, out);

        then(out.toString()).isEqualTo("<html>\n" +
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
            "    <title>Problem Detail: some-title</title>\n" +
            "</head>\n" +
            "<body>\n" +
            "<h1>some-title</h1>\n" +
            "\n" +
            "<table>\n" +
            "    <tr>\n" +
            "        <td>type</td>\n" +
            "        <td>urn:some-type</td>\n" +
            "    </tr>\n" +
            "    <tr>\n" +
            "        <td>title</td>\n" +
            "        <td>some-title</td>\n" +
            "    </tr>\n" +
            "    <tr>\n" +
            "        <td>status</td>\n" +
            "        <td>400</td>\n" +
            "    </tr>\n" +
            "    <tr>\n" +
            "        <td>detail</td>\n" +
            "        <td>some-detail</td>\n" +
            "    </tr>\n" +
            "    <tr>\n" +
            "        <td>instance</td>\n" +
            "        <td>urn:some-instance</td>\n" +
            "    </tr>\n" +
            "    <tr>\n" +
            "        <td>k1</td>\n" +
            "        <td>v1</td>\n" +
            "    </tr>\n" +
            "    <tr>\n" +
            "        <td>k2</td>\n" +
            "        <td>[urn:1, null, urn:2]</td>\n" +
            "    </tr>\n" +
            "    <tr>\n" +
            "        <td>k3</td>\n" +
            "        <td>{k3.1=v3.1, k3.2=v3.2}</td>\n" +
            "    </tr>\n" +
            "    <tr>\n" +
            "        <td>k4</td>\n" +
            "        <td>{k4.1=[v4.1.1, v4.1.2, v4.1.3], k4.2=[v4.2.1], k4.3=[v4.3.1, v4.3.2], k4.4=[]}</td>\n" +
            "    </tr>\n" +
            "</table>\n" +
            "</body>\n" +
            "</html>\n");
    }

    @Test void shouldSerializeAsYaml() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        new YamlMessageBodyWriter().writeTo(SOME_PROBLEM_DETAIL, null, null, null, null, null, out);

        then(out.toString()).isEqualTo("" +
            "type: urn:some-type\n" +
            "title: some-title\n" +
            "status: 400\n" +
            "detail: some-detail\n" +
            "instance: urn:some-instance\n" +
            "k1: v1\n" +
            "k2: ['urn:1', null, 'urn:2']\n" +
            "k3: {k3.1: v3.1, k3.2: v3.2}\n" +
            "k4:\n" +
            "  k4.1: [v4.1.1, v4.1.2, v4.1.3]\n" +
            "  k4.2: [v4.2.1]\n" +
            "  k4.3: [v4.3.1, v4.3.2]\n" +
            "  k4.4: []\n" +
            "k5: null\n");
    }

    private static final Jsonb JSONB = JsonbBuilder.create(new JsonbConfig().withFormatting(true));
}
