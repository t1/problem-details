package test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.assertj.core.api.BDDAssertions.then;
import static org.eclipse.microprofile.problemdetails.Constants.PROBLEM_DETAIL_JSON;
import static test.DemoContainerLaunchingExtension.post;

/** Status codes without a problem detail body */
@ExtendWith(DemoContainerLaunchingExtension.class)
class RawStatusMappingIT {
    @Test void shouldMapRawUnauthorized() {
        Response response = post("raw/401");

        then(response.getStatusInfo()).isEqualTo(UNAUTHORIZED);
        if (response.hasEntity())
            then(response.readEntity(String.class)).isEmpty();
        MediaType contentType = MediaType.valueOf(response.getHeaderString("Content-Type"));
        then(contentType.isCompatible(MediaType.valueOf(PROBLEM_DETAIL_JSON))).isFalse();
    }
}
