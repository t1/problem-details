package test;

import com.github.t1.jaxrslog.LoggingFilter;
import com.github.t1.problemdetail.ri.ProblemDetailClientResponseFilter;
import com.github.t1.problemdetail.ri.lib.ProblemDetailExceptionRegistry;
import com.github.t1.problemdetaildemoapp.DemoService.CreditCardLimitExceeded;
import com.github.t1.problemdetaildemoapp.DemoService.UserNotEntitledToOrderOnAccount;
import com.github.t1.problemdetaildemoapp.OutOfCreditException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.BDDAssertions.then;
import static test.ContainerLaunchingExtension.target;

/**
 * Demonstrate the client side when mapping exceptions to problem details
 * as presented in the rfc.
 */
@Slf4j
@ExtendWith(ContainerLaunchingExtension.class)
class ClientDemoIT extends AbstractClientDemoIT {
    static {
        ProblemDetailExceptionRegistry.register(OutOfCreditException.class);
        ProblemDetailExceptionRegistry.register(CreditCardLimitExceeded.class);
        ProblemDetailExceptionRegistry.register(UserNotEntitledToOrderOnAccount.class);
    }

    @Override protected Shipment postOrder(String userId, String article, String paymentMethod) {
        log.info("post order [{}:{}:{}]", userId, article, paymentMethod);

        try {
            Response response = target()
                .register(LoggingFilter.toStdErr())
                .register(ProblemDetailClientResponseFilter.class)
                .path("/orders").request(APPLICATION_JSON_TYPE)
                .post(Entity.form(new Form()
                    .param("user", userId)
                    .param("article", article)
                    .param("payment-method", paymentMethod)));
            then(response.getStatusInfo()).isEqualTo(OK);
            return response.readEntity(Shipment.class);
        } catch (ResponseProcessingException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Error)
                throw (Error) cause;
            throw (RuntimeException) cause;
        }
    }
}
