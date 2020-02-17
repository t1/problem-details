package test;

import com.github.t1.problemdetails.ri.lib.ProblemDetailExceptionRegistry;
import com.github.t1.problemdetails.spring.ProblemDetailErrorHandler;
import com.github.t1.problemdetaildemoapp.DemoService.CreditCardLimitExceeded;
import com.github.t1.problemdetaildemoapp.DemoService.UserNotEntitledToOrderOnAccount;
import com.github.t1.problemdetaildemoapp.OutOfCreditException;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static test.ContainerLaunchingExtension.BASE_URI;

/**
 * Demonstrate the RI client side when mapping problem details back to exceptions
 */
class ClientDemoIT extends AbstractClientDemoIT {
    static {
        ProblemDetailExceptionRegistry.register(UserNotEntitledToOrderOnAccount.class);
        ProblemDetailExceptionRegistry.register(UserNotEntitledToOrderOnAccount.class, URI.create(
            "https://api.myshop.example/apidocs/com/github/t1/problemdetaildemoapp/DemoService.UserNotEntitledToOrderOnAccount.html"));

        ProblemDetailExceptionRegistry.register(CreditCardLimitExceeded.class);
        ProblemDetailExceptionRegistry.register(CreditCardLimitExceeded.class, URI.create(
            "https://api.myshop.example/apidocs/com/github/t1/problemdetaildemoapp/DemoService.CreditCardLimitExceeded.html"));

        ProblemDetailExceptionRegistry.register(OutOfCreditException.class);
    }

    @Override
    protected Shipment postOrder(String userId, String article, String paymentMethod) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("user", userId);
        form.add("article", article);
        form.add("payment-method", paymentMethod);

        RestTemplate template = new RestTemplate();
        template.setErrorHandler(new ProblemDetailErrorHandler());
        return template.postForObject(BASE_URI + "/orders", form, Shipment.class);
    }

    @Override protected String unknownArticleBody() {
        return "{" +
            "\"type\":\"urn:problem-type:not-found\"," +
            "\"title\":\"Not Found\"," +
            "\"status\":404," +
            "\"instance\":"; // random uuid
    }
}
