package test;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.t1.problemdetaildemoapp.OutOfCreditException;
import com.github.t1.problemdetaildemoapp.ProblemDetail;
import com.github.t1.problemdetaildemoapp.RawDemoBoundary.OutOfCreditProblemDetail;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalDate;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.github.t1.problemdetaildemoapp.DemoService.ACCOUNT_1;
import static com.github.t1.problemdetaildemoapp.DemoService.ACCOUNT_2;
import static com.github.t1.problemdetaildemoapp.DemoService.PROBLEM_INSTANCE;
import static com.github.t1.problemdetaildemoapp.RawDemoBoundary.PROBLEM_DETAIL;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.BDDAssertions.then;
import static test.ContainerLaunchingExtension.BASE_URI;

/**
 * Demonstrate the client side when mapping exceptions to problem details
 * as presented in the rfc.
 */
@Slf4j
@ExtendWith(ContainerLaunchingExtension.class)
class RawSpringDemoIT {

    @Test void shouldOrderCheapGadget() {
        Shipment shipment = postOrder("cheap gadget");

        then(shipment).isEqualTo(new Shipment(
            "1:cheap gadget:" + LocalDate.now(),
            "cheap gadget",
            1));
    }

    @Test void shouldFailToOrderExpensiveGadgetWhenOutOfCredit() {
        OutOfCreditException throwable = catchThrowableOfType(() -> postOrder("expensive gadget"),
            OutOfCreditException.class);

        then(throwable).describedAs("nothing thrown").isNotNull();
        then(throwable.getBalance()).isEqualTo(30);
        then(throwable.getCost()).isEqualTo(0); // not an extension, i.e. not in the body
        then(throwable.getInstance()).isEqualTo(PROBLEM_INSTANCE);
        // detail is not settable, i.e. it's recreated in the method and the cost is 0
        then(throwable.getDetail()).isEqualTo("Your current balance is 30, but that costs 0.");
        then(throwable.getAccounts()).containsExactly(ACCOUNT_1, ACCOUNT_2);
    }

    private Shipment postOrder(String article) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("user", "1");
        form.add("article", article);

        RestTemplate template = new RestTemplate();
        try {
            return template.postForObject(BASE_URI + "/orders-raw", form, Shipment.class);
        } catch (HttpStatusCodeException e) {
            ProblemDetail problemDetail = readProblemDetail(e);
            if (problemDetail == null)
                throw e;
            switch (problemDetail.getType().toString()) {
                case "https://example.com/probs/out-of-credit": {
                    OutOfCreditProblemDetail detail = requireNonNull(readJson(e, OutOfCreditProblemDetail.class));
                    throw new OutOfCreditException(
                        detail.getInstance(),
                        detail.getBalance(),
                        0,
                        detail.getAccounts()
                    );
                }
                case "foo":
                    throw new RuntimeException();
                default:
                    log.warn("unknown problem detail type" + problemDetail.getType() + ":\n" + problemDetail);
                    throw e;
            }
        }
    }

    private ProblemDetail readProblemDetail(HttpStatusCodeException exception) {
        if (!isProblemDetail(exception))
            return null;
        return readJson(exception, ProblemDetail.class);
    }

    private boolean isProblemDetail(HttpStatusCodeException exception) {
        HttpHeaders headers = exception.getResponseHeaders();
        return headers != null && PROBLEM_DETAIL.isCompatibleWith(headers.getContentType());
    }

    private <T extends ProblemDetail> T readJson(HttpStatusCodeException exception, Class<T> type) {
        ObjectMapper mapper = new ObjectMapper().disable(FAIL_ON_UNKNOWN_PROPERTIES);
        try {
            return mapper.readValue(exception.getResponseBodyAsByteArray(), type);
        } catch (IOException e) {
            log.warn("can't deserialize problem detail: " + exception.getResponseBodyAsString(), e);
            return null;
        }
    }

    @AllArgsConstructor @NoArgsConstructor
    public static @Data class Shipment {
        @JsonProperty("shipment-id") String shipmentId;
        String article;
        Integer user;
    }
}
