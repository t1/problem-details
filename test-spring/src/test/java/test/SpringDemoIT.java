package test;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.t1.problemdetaildemoapp.OutOfCreditException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.net.URI;
import java.time.LocalDate;

import static com.github.t1.problemdetaildemoapp.DemoBoundary.ACCOUNT_1;
import static com.github.t1.problemdetaildemoapp.DemoBoundary.ACCOUNT_2;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.BDDAssertions.then;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static test.ContainerLaunchingExtension.BASE_URI;
import static test.ContainerLaunchingExtension.createRestTemplate;

/**
 * Demonstrate the client side when mapping exceptions to problem details
 * as presented in the rfc.
 */
@ExtendWith(ContainerLaunchingExtension.class)
class SpringDemoIT {

    @Test void shouldOrderCheapGadget() {
        ResponseEntity<Shipment> response = postOrder("cheap gadget");

        then(response.getStatusCode()).isEqualTo(OK);
        then(response.getBody()).isEqualTo(new Shipment(
            "1:cheap gadget:" + LocalDate.now(),
            "cheap gadget",
            1));
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

    private ResponseEntity<Shipment> postOrder(String article) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("user", "1");
        form.add("article", article);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_FORM_URLENCODED);

        return createRestTemplate(APPLICATION_FORM_URLENCODED, APPLICATION_JSON).postForEntity(BASE_URI + "/orders",
            new HttpEntity<>(form, headers), Shipment.class);
    }

    @AllArgsConstructor @NoArgsConstructor
    public static @Data class Shipment {
        @JsonProperty("shipment-id") String shipmentId;
        String article;
        Integer user;
    }
}
