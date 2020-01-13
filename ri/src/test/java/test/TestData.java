package test;

import com.github.t1.problemdetail.Detail;
import com.github.t1.problemdetail.Extension;
import com.github.t1.problemdetail.Instance;
import com.github.t1.problemdetail.Status;
import com.github.t1.problemdetail.Title;
import com.github.t1.problemdetail.Type;
import com.github.t1.problemdetail.ri.lib.ProblemDetails;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

class TestData {
    @Type("urn:some-type")
    @Title("some-title")
    @Status(BAD_REQUEST)
    public static class SomeException extends RuntimeException {
        @Detail String detail = "some-detail";
        @Instance URI instance = URI.create("urn:some-instance");
        @Extension String k1 = "v1";
        @Extension List<URI> k2 = asList(URI.create("urn:1"), null, URI.create("urn:2"));
        @Extension Map<String, Object> k3 = v3();
        @Extension Map<String, Object> k4 = v4();
        @Extension String k5 = null;
    }

    static final ProblemDetails PROBLEM_DETAILS = new ProblemDetails(new SomeException()) {
        @Override protected boolean hasDefaultMessage() { return false; }

        @Override protected String findMediaTypeSubtype() { return null; }
    };


    static Map<String, Object> v3() {
        Map<String, Object> v3 = new LinkedHashMap<>();
        v3.put("k3.1", "v3.1");
        v3.put("k3.2", "v3.2");
        return v3;
    }

    static Map<String, Object> v4() {
        Map<String, Object> v4 = new LinkedHashMap<>();
        v4.put("k4.1", asList("v4.1.1", "v4.1.2", "v4.1.3"));
        v4.put("k4.2", singletonList("v4.2.1"));
        v4.put("k4.3", asList("v4.3.1", "v4.3.2"));
        v4.put("k4.4", emptyList());
        return v4;
    }
}
