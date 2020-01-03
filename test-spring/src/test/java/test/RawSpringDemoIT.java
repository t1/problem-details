package test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.t1.problemdetaildemoapp.DemoService.UserNotEntitledToOrderOnAccount;
import com.github.t1.problemdetaildemoapp.OutOfCreditException;
import com.github.t1.problemdetaildemoapp.ProblemDetail;
import com.github.t1.problemdetaildemoapp.RawDemoBoundary.OutOfCreditProblemDetail;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException.NotFound;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.github.t1.problemdetaildemoapp.ProblemDetail.JSON_MEDIA_TYPE;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.BDDAssertions.then;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static test.ContainerLaunchingExtension.BASE_URI;

/**
 * Demonstrate the manual client side mapping of problem details to exceptions without library support
 */
@Slf4j
class RawSpringDemoIT extends AbstractSpringDemoIT {
    @Override
    protected Shipment postOrder(String userId, String article, String paymentMethod) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("user", userId);
        form.add("article", article);
        form.add("payment-method", paymentMethod);

        RestTemplate template = new RestTemplate();
        try {
            return template.postForObject(BASE_URI + "/orders-raw", form, Shipment.class);
        } catch (HttpStatusCodeException e) {
            ProblemDetail problemDetail = readProblemDetail(e);
            if (problemDetail == null)
                throw e;
            URI type = problemDetail.getType();
            if (type == null) {
                log.warn("no problem detail type in:\n" + problemDetail);
                throw e;
            }
            switch (type.toString()) {
                case "https://example.com/probs/out-of-credit": {
                    OutOfCreditProblemDetail detail = requireNonNull(readJson(e, OutOfCreditProblemDetail.class));
                    throw new OutOfCreditException(
                        detail.getInstance(),
                        detail.getBalance(),
                        0,
                        detail.getAccounts()
                    );
                }

                case "https://api.myshop.example/problems/not-entitled-for-payment-method":
                    throw new UserNotEntitledToOrderOnAccount();

                default:
                    log.warn("unknown problem detail type [" + type + "]:\n" + problemDetail);
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
        return headers != null && JSON_MEDIA_TYPE.isCompatibleWith(headers.getContentType());
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

    @Test void shouldFailToOrderUnknownArticle() {
        NotFound throwable = catchThrowableOfType(() -> postOrder("1", "unknown article", null),
            NotFound.class);

        then(throwable).describedAs("nothing thrown").isNotNull();
        then(throwable.getStatusCode()).isEqualTo(NOT_FOUND);
        then(throwable.getMessage())
            .contains("\"type\":\"https://api.myshop.example/problems/com/github/t1/problemdetaildemoapp/DemoService.ArticleNotFoundException.html\"")
            .contains("\"title\":\"ArticleNotFoundException\"")
            .contains("\"detail\":null")
            .contains("\"status\":404")
            .contains("\"instance\":\""); // + random uuid
    }
}
