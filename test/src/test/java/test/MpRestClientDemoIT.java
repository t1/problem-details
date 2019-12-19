package test;

import com.github.t1.problemdetail.ri.ProblemDetailResponseExceptionMapper;
import com.github.t1.problemdetail.ri.lib.ProblemDetailExceptionRegistry;
import com.github.t1.problemdetail.ri.lib.ProblemDetailJsonToExceptionBuilder;
import com.github.t1.problemdetaildemoapp.LoggingFilter;
import com.github.t1.problemdetaildemoapp.OutOfCreditException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.json.bind.annotation.JsonbProperty;
import javax.validation.constraints.NotNull;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.net.URI;
import java.time.LocalDate;

import static com.github.t1.problemdetaildemoapp.DemoBoundary.ACCOUNT_1;
import static com.github.t1.problemdetaildemoapp.DemoBoundary.ACCOUNT_2;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.BDDAssertions.then;
import static test.ContainerLaunchingExtension.baseUri;

@ExtendWith(ContainerLaunchingExtension.class)
public class MpRestClientDemoIT {
    static {
        ProblemDetailExceptionRegistry.register(OutOfCreditException.class);
    }

    @Path("/orders")
    public interface OrderApi {
        @POST Shipment order(
            @FormParam("user") int userId,
            @FormParam("article") @NotNull String article);
    }

    @AllArgsConstructor @NoArgsConstructor
    public static @Data class Shipment {
        @JsonbProperty("shipment-id") String shipmentId;
        @JsonbProperty("user") int userId;
        String article;
    }

    private OrderApi api = RestClientBuilder.newBuilder()
        .baseUri(baseUri(""))
        .register(ProblemDetailResponseExceptionMapper.class)
        .register(LoggingFilter.toStdErr())
        .build(OrderApi.class);

    @Test void shouldOrderCheapGadget() {
        Shipment shipment = api.order(1, "cheap gadget");

        then(shipment).isEqualTo(new Shipment(
            "1:cheap gadget:" + LocalDate.now(),
            1,
            "cheap gadget"));
    }

    @Test void shouldFailToOrderExpensiveGadgetWhenOutOfCredit() {
        OutOfCreditException throwable = catchThrowableOfType(() -> api.order(1, "expensive gadget"),
            OutOfCreditException.class);

        then(throwable).describedAs("nothing thrown").isNotNull();
        then(throwable.getBalance()).isEqualTo(30);
        then(throwable.getCost()).isEqualTo(0); // not an extension, i.e. not in the body
        then(throwable.getInstance()).isEqualTo(URI.create("/account/12345/msgs/abc"));
        // detail is not settable, i.e. it's recreated in the method and the cost is 0
        then(throwable.getDetail()).isEqualTo("Your current balance is 30, but that costs 0.");
        then(throwable.getAccounts()).containsExactly(ACCOUNT_1, ACCOUNT_2);
    }
}
