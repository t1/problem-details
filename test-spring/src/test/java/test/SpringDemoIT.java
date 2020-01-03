package test;

import com.github.t1.problemdetail.ri.lib.ProblemDetailExceptionRegistry;
import com.github.t1.problemdetail.spring.ProblemDetailErrorHandler;
import com.github.t1.problemdetaildemoapp.DemoService.ArticleNotFoundException;
import com.github.t1.problemdetaildemoapp.DemoService.UserNotEntitledToOrderOnAccount;
import com.github.t1.problemdetaildemoapp.OutOfCreditException;
import org.junit.jupiter.api.Test;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.BDDAssertions.then;
import static test.ContainerLaunchingExtension.BASE_URI;

/**
 * Demonstrate the RI client side when mapping problem details back to exceptions
 */
class SpringDemoIT extends AbstractSpringDemoIT {
    static {
        ProblemDetailExceptionRegistry.register(UserNotEntitledToOrderOnAccount.class);
        ProblemDetailExceptionRegistry.register(ArticleNotFoundException.class, URI.create(
            "https://api.myshop.example/problems/com/github/t1/problemdetaildemoapp/DemoService.ArticleNotFoundException.html"));
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

    @Test void shouldFailToOrderUnknownArticle() {
        ArticleNotFoundException throwable = catchThrowableOfType(() -> postOrder("1", "unknown article", null),
            ArticleNotFoundException.class);

        then(throwable).describedAs("nothing thrown").isNotNull();
        // then(throwable.getArticle()).isEqualTo("unknown article");
    }
}
