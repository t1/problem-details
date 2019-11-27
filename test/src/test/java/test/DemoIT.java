package test;

import com.github.t1.problemdetail.ProblemDetail;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
import java.math.BigInteger;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import static com.github.t1.problemdetail.Constants.PROBLEM_DETAIL_JSON_TYPE;
import static com.github.t1.problemdetaildemoapp.DemoBoundary.ACCOUNT_1;
import static com.github.t1.problemdetaildemoapp.DemoBoundary.ACCOUNT_2;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.BDDAssertions.then;
import static test.ProblemDetailMapperExtension.then;

/**
 * Demonstrate the behavior of mapping exceptions to problem details
 * as presented on the rfc.
 */
class DemoIT {
    @RegisterExtension static ProblemDetailMapperExtension mapper = new ProblemDetailMapperExtension();

    @Test void shouldOrderCheapGadget() {
        Response response = mapper.post("/orders", Entity.form(new Form()
            .param("user", "1")
            .param("article", "cheap gadget")));

        then(response.getStatusInfo()).isEqualTo(OK);
        then(response.readEntity(String.class)).isEqualTo("" +
            "{\"shipment-id\":\"1:cheap gadget:" + LocalDate.now() + "\",\"article\":\"cheap gadget\",\"user\":1}");
    }

    @Test void shouldFailToOrderExpensiveGadgetWhenOutOfCredit() {
        Response response = mapper.post("/orders", Entity.form(new Form()
            .param("user", "1")
            .param("article", "expensive gadget")));

        response.bufferEntity();
        System.out.println(response.readEntity(String.class));

        then(response, ExtendedProblemDetail.class)
            .hasStatus(FORBIDDEN)
            .hasMediaType(PROBLEM_DETAIL_JSON_TYPE)
            .hasType("https://example.com/probs/out-of-credit")
            .hasTitle("You do not have enough credit.")
            .hasDetail("Your current balance is 30, but that costs 50.")
            .hasInstance(URI.create("/account/12345/msgs/abc"))
            .checkExtensions(extended -> {
                then(extended.balance).describedAs("problem-detail.balance")
                    .isEqualTo(30);
                then(extended.accounts).describedAs("problem-detail.accounts")
                    .containsExactly(ACCOUNT_1, ACCOUNT_2);
            });
    }

    @Data @EqualsAndHashCode(callSuper = true)
    public static class ExtendedProblemDetail extends ProblemDetail {
        private BigInteger balance;
        private List<URI> accounts;
    }
}
