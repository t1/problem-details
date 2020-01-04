package com.github.t1.problemdetaildemoapp;

import com.github.t1.problemdetail.Detail;
import com.github.t1.problemdetail.Extension;
import com.github.t1.problemdetail.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.json.bind.annotation.JsonbProperty;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.net.URI;
import java.time.LocalDate;

import static java.util.Arrays.asList;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;

@Slf4j
@Path("/orders")
public class DemoBoundary {
    @POST public Shipment order(
        @FormParam("user") int userId,
        @FormParam("article") @NotNull String article,
        @FormParam(value = "payment-method") @DefaultValue("prepaid") PaymentMethod paymentMethod) {

        log.info("order [{}] for [{}] via [{}]", article, userId, paymentMethod);

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
                throw new NotFoundException("There is no article [" + article + "]");
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

    @Status(FORBIDDEN) public static class CreditCardLimitExceeded extends RuntimeException {}

    @Status(FORBIDDEN) public static class UserNotEntitledToOrderOnAccount extends RuntimeException {}

    @AllArgsConstructor @NoArgsConstructor
    public static @Data class Shipment {
        @JsonbProperty("shipment-id") String shipmentId;
        @JsonbProperty("user") int userId;
        String article;
    }
}
