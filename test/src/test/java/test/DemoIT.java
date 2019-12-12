package test;

import com.github.t1.problemdetaildemoapp.OutOfCreditException;
import com.github.t1.problemdetailmapper.ProblemDetailHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.time.LocalDate;

import static com.github.t1.problemdetaildemoapp.DemoBoundary.ACCOUNT_1;
import static com.github.t1.problemdetaildemoapp.DemoBoundary.ACCOUNT_2;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.BDDAssertions.then;
import static test.ContainerLaunchingExtension.target;

/**
 * Demonstrate the client side when mapping exceptions to problem details
 * as presented in the rfc.
 */
@ExtendWith(ContainerLaunchingExtension.class)
class DemoIT {
    static {
        ProblemDetailHandler.CONFIG.put("https://example.com/probs/out-of-credit", OutOfCreditException.class);
    }

    @Test void shouldOrderCheapGadget() {
        Response response = postOrder("cheap gadget");

        then(response.getStatusInfo()).isEqualTo(OK);
        then(response.readEntity(String.class)).isEqualTo("" +
            "{\"shipment-id\":\"1:cheap gadget:" + LocalDate.now() + "\",\"article\":\"cheap gadget\",\"user\":1}");
    }

    @Test void shouldFailToOrderExpensiveGadgetWhenOutOfCredit() {
        OutOfCreditException throwable = catchThrowableOfType(() -> postOrder("expensive gadget"),
            OutOfCreditException.class);

        then(throwable.getBalance()).isEqualTo(30);
        then(throwable.getCost()).isEqualTo(0); // not an extension, i.e. not in the body
        then(throwable.getInstance()).isEqualTo(URI.create("/account/12345/msgs/abc"));
        // detail is not settable, i.e. it's recreated in the method and the cost is 0
        then(throwable.getDetail()).isEqualTo("Your current balance is 30, but that costs 0.");
        then(throwable.getAccounts()).containsExactly(ACCOUNT_1, ACCOUNT_2);
    }

    private Response postOrder(String article) {
        try {
            return target()
                .register(ProblemDetailHandler.class) // this would be registered globally
                .path("/orders").request(APPLICATION_JSON_TYPE)
                .post(Entity.form(new Form()
                    .param("user", "1")
                    .param("article", article)));
        } catch (ResponseProcessingException e) {
            throw (RuntimeException) e.getCause();
        }
    }
}
