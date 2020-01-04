package test;

import com.github.t1.problemdetaildemoapp.DemoService.CreditCardLimitExceeded;
import com.github.t1.problemdetaildemoapp.DemoService.UserNotEntitledToOrderOnAccount;
import com.github.t1.problemdetaildemoapp.OutOfCreditException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import javax.json.bind.annotation.JsonbProperty;
import javax.ws.rs.NotFoundException;
import java.time.LocalDate;

import static com.github.t1.problemdetaildemoapp.DemoService.ACCOUNT_1;
import static com.github.t1.problemdetaildemoapp.DemoService.ACCOUNT_2;
import static com.github.t1.problemdetaildemoapp.DemoService.PROBLEM_INSTANCE;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.assertj.core.api.BDDAssertions.then;

public abstract class AbstractClientDemoIT {
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
        then(throwable.getMessage()).isEqualTo("not really");
    }

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
    }

    @Test void shouldFailToOrderWhenCreditCardLimitIsReached() {
        CreditCardLimitExceeded throwable = catchThrowableOfType(() -> postOrder("1", "expensive gadget", "credit_card"),
            CreditCardLimitExceeded.class);

        then(throwable).describedAs("nothing thrown").isNotNull();
    }

    /** standard JAX-RS exception */
    @Test void shouldFailToOrderUnknownArticle() {
        NotFoundException throwable = catchThrowableOfType(() -> postOrder("1", "unknown article", null),
            NotFoundException.class);

        then(throwable).describedAs("nothing thrown").isNotNull();
        then(throwable.getMessage()).startsWith("There is no article [unknown article]");
    }


    protected abstract Shipment postOrder(String userId, String article, String paymentMethod);

    @AllArgsConstructor @NoArgsConstructor
    public static @Data class Shipment {
        @JsonbProperty("shipment-id") String shipmentId;
        @JsonbProperty("user") int userId;
        String article;
    }
}
