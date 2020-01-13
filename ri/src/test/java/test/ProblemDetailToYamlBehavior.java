package test;

import com.github.t1.problemdetail.ri.YamlMessageBodyWriter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.Map;

import static org.assertj.core.api.BDDAssertions.then;
import static test.TestData.PROBLEM_DETAILS;

public class ProblemDetailToYamlBehavior {
    @Test void shouldSerializeAsYaml() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        @SuppressWarnings("unchecked") Map<String, Object> body = (Map<String, Object>) PROBLEM_DETAILS.getBody();

        new YamlMessageBodyWriter().writeTo(body, null, null, null, null, null, out);

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
}
