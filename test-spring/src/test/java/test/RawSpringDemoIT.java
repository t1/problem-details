package test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.t1.problemdetaildemoapp.DemoService.CreditCardLimitExceeded;
import com.github.t1.problemdetaildemoapp.DemoService.UserNotEntitledToOrderOnAccount;
import com.github.t1.problemdetaildemoapp.OutOfCreditException;
import com.github.t1.problemdetaildemoapp.ProblemDetail;
import com.github.t1.problemdetaildemoapp.RawDemoBoundary.OutOfCreditProblemDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static java.util.Objects.requireNonNull;
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
            if (!isProblemDetail(e))
                throw e;
            String json = e.getResponseBodyAsString();
            ProblemDetail problemDetail = readJson(json, ProblemDetail.class);
            if (problemDetail == null) {
                log.warn("can't deserialize problem detail: " + json, e);
                throw e;
            }
            URI type = problemDetail.getType();
            if (type == null) {
                log.warn("no problem detail type in:\n" + problemDetail);
                throw e;
            }

            log.info("got {}", problemDetail);

            switch (type.toString()) {
                case "https://example.com/probs/out-of-credit": {
                    OutOfCreditProblemDetail detail = requireNonNull(readJson(json, OutOfCreditProblemDetail.class));
                    throw new OutOfCreditException(
                        detail.getInstance(),
                        detail.getBalance(),
                        0,
                        detail.getAccounts()
                    );
                }

                case "https://api.myshop.example/problems/not-entitled-for-payment-method":
                case "https://api.myshop.example/problems/com/github/t1/problemdetaildemoapp/DemoService.UserNotEntitledToOrderOnAccount.html":
                    throw new UserNotEntitledToOrderOnAccount();

                case "urn:problem-type:credit-card-limit-exceeded":
                case "https://api.myshop.example/problems/com/github/t1/problemdetaildemoapp/DemoService.CreditCardLimitExceeded.html":
                    throw new CreditCardLimitExceeded();

                case "urn:problem-type:article-not-found":
                case "https://api.myshop.example/problems/com/github/t1/problemdetaildemoapp/DemoService.ArticleNotFoundException.html":
                    throw new ArticleNotFoundException();

                default:
                    log.warn("unknown problem detail type [" + type + "]:\n" + json);
                    throw e;
            }
        }
    }

    private boolean isProblemDetail(HttpStatusCodeException exception) {
        HttpHeaders headers = exception.getResponseHeaders();
        return headers != null && ProblemDetail.JSON_MEDIA_TYPE.isCompatibleWith(headers.getContentType());
    }

    private <T extends ProblemDetail> T readJson(String json, Class<T> type) {
        ObjectMapper mapper = new ObjectMapper().disable(FAIL_ON_UNKNOWN_PROPERTIES);
        try {
            return mapper.readValue(json, type);
        } catch (IOException e) {
            return null;
        }
    }

    @Override protected String unknownArticleBody() {
        return "{" +
            "\"type\":\"urn:problem-type:not-found\"," +
            "\"title\":\"Not Found\"," +
            "\"status\":404," +
            "\"detail\":\"There is no article [unknown article]\"," +
            "\"instance\":"; // random uuid
    }
}
