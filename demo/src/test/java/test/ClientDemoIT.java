package test;

import com.github.t1.jaxrslog.LoggingFilter;
import com.github.t1.problemdetaildemoapp.DemoService.CreditCardLimitExceeded;
import com.github.t1.problemdetaildemoapp.DemoService.UserNotEntitledToOrderOnAccount;
import com.github.t1.problemdetaildemoapp.OutOfCreditException;
import com.github.t1.problemdetails.ri.ProblemDetailClientResponseFilter;
import com.github.t1.problemdetails.ri.lib.ProblemDetailExceptionRegistry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.json.bind.annotation.JsonbProperty;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
import java.time.LocalDate;

import static com.github.t1.problemdetaildemoapp.DemoService.ACCOUNT_1;
import static com.github.t1.problemdetaildemoapp.DemoService.ACCOUNT_2;
import static com.github.t1.problemdetaildemoapp.DemoService.PROBLEM_INSTANCE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.assertj.core.api.BDDAssertions.then;
import static org.eclipse.microprofile.problemdetails.LogLevel.ERROR;
import static org.eclipse.microprofile.problemdetails.LogLevel.INFO;
import static test.DemoContainerLaunchingExtension.assumeCanCheckLogging;
import static test.DemoContainerLaunchingExtension.target;
import static test.DemoContainerLaunchingExtension.thenLogged;

/**
 * Demonstrate the client side when mapping exceptions to problem details
 * as presented in the rfc.
 */
@Slf4j
@ExtendWith(DemoContainerLaunchingExtension.class)
class ClientDemoIT {
    static {
        ProblemDetailExceptionRegistry.register(OutOfCreditException.class);
        ProblemDetailExceptionRegistry.register(CreditCardLimitExceeded.class);
        ProblemDetailExceptionRegistry.register(UserNotEntitledToOrderOnAccount.class);
    }

    @Test void shouldOrderCheapGadget() {
        Shipment shipment = postOrder("1", "cheap gadget", null);

        then(shipment).isEqualTo(new Shipment(
            "1:cheap gadget:" + LocalDate.now(),
            1,
            "cheap gadget"));
    }

    @Test void shouldFailToOrderGadgetWhenUserNotEntitledToOrderOnAccount() {
        UserNotEntitledToOrderOnAccount throwable = catchThrowableOfType(() -> postOrder("2", "cheap gadget", "on_account"),
            UserNotEntitledToOrderOnAccount.class);

        then(throwable).describedAs("nothing thrown").isNotNull();
    }

    @Test void shouldFailToOrderOutOfMemoryBomb() {
        OutOfMemoryError throwable = catchThrowableOfType(() -> postOrder("1", "oom bomb", null),
            OutOfMemoryError.class);

        then(throwable).describedAs("nothing thrown").isNotNull();
        assumeCanCheckLogging();
        thenLogged(ERROR, OutOfMemoryError.class.getName())
            .type("urn:problem-type:out-of-memory-error")
            .title("Out Of Memory Error")
            .status("500")
            .instance("urn:uuid:")
            .stackTrace("java.lang.OutOfMemoryError: not really")
            .check();
    }

    // TODO TomEE explodes the accounts-uris https://github.com/t1/problem-details/issues/17
    @DisabledIfSystemProperty(named = "jee-testcontainer", matches = "tomee")
    @Test void shouldFailToOrderExpensiveGadgetWhenOutOfCredit() {
        OutOfCreditException throwable = catchThrowableOfType(() -> postOrder("1", "expensive gadget", null),
            OutOfCreditException.class);

        then(throwable).describedAs("nothing thrown").isNotNull();
        assumeThat(throwable.getBalance()) // eventually finishes the test as ignored
            .describedAs("extension fields are not supported by all demos")
            .isEqualTo(30);
        then(throwable.getCost()).isEqualTo(0); // not an extension, i.e. not in the body
        then(throwable.getInstance()).isEqualTo(PROBLEM_INSTANCE);
        // detail is not settable, i.e. it's recreated in the method and the cost is 0
        then(throwable.getDetail()).isEqualTo("Your current balance is 30, but that costs 0.");
        then(throwable.getAccounts()).containsExactly(ACCOUNT_1, ACCOUNT_2);
        assumeCanCheckLogging();
        thenLogged(INFO, OutOfCreditException.class.getName())
            .type("https://example.com/probs/out-of-credit")
            .title("You do not have enough credit.")
            .status("403")
            .detail("Your current balance is 30, but that costs 50.")
            .instance("/account/12345/msgs/abc")
            .extension("accounts: [/account/12345, /account/67890]")
            .extension("balance: 30")
            .noStackTrace()
            .check();
    }

    @Test void shouldFailToOrderWhenCreditCardLimitIsReached() {
        CreditCardLimitExceeded throwable = catchThrowableOfType(() -> postOrder("1", "expensive gadget", "credit_card"),
            CreditCardLimitExceeded.class);

        then(throwable).describedAs("nothing thrown").isNotNull();
    }

    /** standard JAX-RS exception */
    // TODO TomEE doesn't write some problem detail entities https://github.com/t1/problem-details/issues/17
    @DisabledIfSystemProperty(named = "jee-testcontainer", matches = "tomee")
    @Test void shouldFailToOrderUnknownArticle() {
        NotFoundException throwable = catchThrowableOfType(() -> postOrder("1", "unknown article", null),
            NotFoundException.class);

        then(throwable).describedAs("nothing thrown").isNotNull();
    }


    @AllArgsConstructor @NoArgsConstructor
    public static @Data class Shipment {
        @JsonbProperty("shipment-id") String shipmentId;
        @JsonbProperty("user") int userId;
        String article;
    }

    private Shipment postOrder(String userId, String article, String paymentMethod) {
        log.info("post order [{}:{}:{}]", userId, article, paymentMethod);

        try {
            WebTarget target = target()
                .path("/orders")
                .register(LoggingFilter.toStdErr())
                .register(ProblemDetailClientResponseFilter.class);
            Response response = target.request(APPLICATION_JSON_TYPE)
                .post(Entity.form(new Form()
                    .param("user", userId)
                    .param("article", article)
                    .param("payment-method", paymentMethod)));
            then(response.getStatusInfo()).as("POST on %s", target.getUri()).isEqualTo(OK);
            return response.readEntity(Shipment.class);
        } catch (ResponseProcessingException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Error)
                throw (Error) cause;
            throw (RuntimeException) cause;
        }
    }
}
