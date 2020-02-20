package test;

import com.github.t1.testcontainers.jee.JeeContainer;
import org.eclipse.microprofile.problemdetails.tck.ContainerLaunchingExtension;

import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

class DemoContainerLaunchingExtension extends ContainerLaunchingExtension {
    @Override protected String runningProperty() { return "problem-details-demo-running"; }

    @Override protected JeeContainer buildJeeContainer() {
        return JeeContainer.create().withDeployment("target/problem-details-jaxrs-demo.war");
    }

    public static Response post(String path) {
        return target(path).request(APPLICATION_JSON_TYPE).post(null);
    }
}
