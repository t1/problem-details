package com.github.t1.problemdetaildemoapp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.problemdetails.Status;
import org.eclipse.microprofile.problemdetails.Type;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.net.URI;
import java.time.LocalDate;

import static java.util.Arrays.asList;
import static org.eclipse.microprofile.problemdetails.ResponseStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@Service
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

    private int cost(String article) {
        switch (article) {
            case "oom bomb":
                throw new OutOfMemoryError("not really");
            case "expensive gadget":
                return 50;
            case "cheap gadget":
                return 5;
            default:
                //noinspection ConstantConditions // the `headers` arg is missing `@Nullable`
                throw HttpClientErrorException.create("There is no article [" + article + "]",
                    NOT_FOUND, NOT_FOUND.getReasonPhrase(), null, null, null);
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

    @ResponseStatus(HttpStatus.FORBIDDEN)
    // for the raw handling
    @Status(FORBIDDEN) public static class CreditCardLimitExceeded extends DemoException {}

    @Type("https://api.myshop.example/problems/not-entitled-for-payment-method")
    @Status(FORBIDDEN) public static class UserNotEntitledToOrderOnAccount extends DemoException {}

    public static class DemoException extends RuntimeException {}

    @AllArgsConstructor @NoArgsConstructor
    public static @Data class Shipment {
        @JsonProperty("shipment-id") String shipmentId;
        @JsonProperty("user") int userId;
        String article;
    }
}
