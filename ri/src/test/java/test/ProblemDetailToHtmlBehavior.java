package test;

import com.github.t1.problemdetail.ri.ProblemDetailHtmlMessageBodyWriter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.Map;

import static org.assertj.core.api.BDDAssertions.then;
import static test.TestData.PROBLEM_DETAILS;

class ProblemDetailToHtmlBehavior {
    @Test void shouldSerializeAsHtml() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        @SuppressWarnings("unchecked") Map<String, Object> body = (Map<String, Object>) PROBLEM_DETAILS.getBody();

        new ProblemDetailHtmlMessageBodyWriter().writeTo(body, null, null, null, null, null, out);

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
}
