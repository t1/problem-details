package test;

import com.github.t1.testcontainers.jee.JeeContainer;
import org.eclipse.microprofile.problemdetails.tck.ContainerLaunchingExtension;

class DemoContainerLaunchingExtension extends ContainerLaunchingExtension {
    @Override protected String runningProperty() { return "ri-demo-running"; }

    @Override protected JeeContainer buildJeeContainer() {
        return JeeContainer.create().withDeployment("target/ri-demo.war");
    }
}
