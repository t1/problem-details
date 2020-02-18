package com.github.t1.problemdetaildemoapp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.problemdetails.Status;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.inject.Inject;
import javax.json.bind.annotation.JsonbProperty;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.net.URI;
import java.time.LocalDate;

import static java.util.Arrays.asList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.eclipse.microprofile.problemdetails.ResponseStatus.FORBIDDEN;

@Slf4j
public class DemoService {
    public Shipment order(int userId, String article, PaymentMethod paymentMethod) {
        log.info("order [{}] for [{}] by [{}]", article, userId, paymentMethod);

        int cost = cost(article);

        checkPaymentMethod(userId, cost, paymentMethod);

        deduct(cost, userId);
        String shipmentId = ship(article, userId);
        log.info("ship {} id {} to {}", article, shipmentId, userId);

        return new Shipment(shipmentId, userId, article);
    }


    public enum PaymentMethod {
        prepaid, credit_card, on_account
    }

    private void checkPaymentMethod(int userId, int cost, PaymentMethod paymentMethod) {
        switch (paymentMethod) {
            case prepaid:
                break;
            case credit_card:
                if (cost > 20)
                    throw new CreditCardLimitExceeded();
                break;
            case on_account:
                if (userId == 2)
                    throw new UserNotEntitledToOrderOnAccount();
                break;
        }
    }

    @Path("/prices/{articleId}")
    @RegisterRestClient
    @Produces(APPLICATION_JSON)
    public interface PriceService {
        @GET int get(@PathParam("articleId") String articleId);
    }

    @Inject @RestClient PriceService prices;

    private int cost(String article) {
        if ("oom bomb".equals(article)) {
            throw new OutOfMemoryError("not really");
        }
        return prices.get(article);
    }

    private void deduct(int cost, int userId) {
        int balance = balance(userId);
        if (cost > balance) {
            throw new OutOfCreditException(PROBLEM_INSTANCE, balance, cost, asList(ACCOUNT_1, ACCOUNT_2));
        }
    }

    private int balance(int userId) {
        switch (userId) {
            case 1:
                return 30;
            case 2:
                return 10;
            default:
                throw new IllegalArgumentException("unknown user " + userId);
        }
    }

    private String ship(String article, int userId) {
        return userId + ":" + article + ":" + LocalDate.now();
    }

    /** normally this is never a constant... this is only to make testing easier */
    public static final URI PROBLEM_INSTANCE = URI.create("/account/12345/msgs/abc");

    public static final URI ACCOUNT_1 = URI.create("/account/12345");
    public static final URI ACCOUNT_2 = URI.create("/account/67890");

    @Status(FORBIDDEN) public static class CreditCardLimitExceeded extends DemoException {}

    @Status(FORBIDDEN) public static class UserNotEntitledToOrderOnAccount extends DemoException {}

    public static class DemoException extends RuntimeException {}

    @AllArgsConstructor @NoArgsConstructor
    public static @Data class Shipment {
        @JsonbProperty("shipment-id") String shipmentId;
        @JsonbProperty("user") int userId;
        String article;
    }
}
