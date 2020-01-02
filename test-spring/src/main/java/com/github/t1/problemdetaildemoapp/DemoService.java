package com.github.t1.problemdetaildemoapp;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.t1.problemdetail.Detail;
import com.github.t1.problemdetail.Extension;
import com.github.t1.problemdetail.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.LocalDate;

import static java.util.Arrays.asList;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;

@Slf4j
@Service
public class DemoService {

    public Shipment order(int userId, String article, PaymentMethod paymentMethod) {
        log.info("order {} for {} via {}", article, userId, paymentMethod);

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
                if (cost > 1000)
                    throw new CreditCardLimitExceeded();
                break;
            case on_account:
                if (userId == 2)
                    throw new UserNotEntitledToOrderOnAccount();
                break;
        }
    }

    private int cost(String article) {
        switch (article) {
            case "expensive gadget":
                return 50;
            case "cheap gadget":
                return 5;
            default:
                throw new ArticleNotFoundException(article);
        }
    }

    private void deduct(int cost, int userId) {
        int balance = balance(userId);
        if (balance < cost) {
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

    @Status(FORBIDDEN) private static class CreditCardLimitExceeded extends RuntimeException {}

    @Status(FORBIDDEN) private static class UserNotEntitledToOrderOnAccount extends RuntimeException {}

    @AllArgsConstructor @NoArgsConstructor
    private static class ArticleNotFoundException extends IllegalArgumentException {
        @Extension String article;

        @Detail String getDetail() { return "The article " + article + " is not in our catalog"; }
    }

    @AllArgsConstructor @NoArgsConstructor
    public static @Data class Shipment {
        @JsonProperty("shipment-id") String shipmentId;
        @JsonProperty("user") int userId;
        String article;
    }
}
