package com.github.t1.problemdetaildemoapp;

import lombok.extern.slf4j.Slf4j;

import javax.json.Json;
import javax.json.JsonObject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.FormParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.net.URI;
import java.time.LocalDate;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@Path("/orders")
public class DemoBoundary {
    @Produces(APPLICATION_JSON)
    @POST public JsonObject order(
        @FormParam("user") int userId,
        @FormParam("article") @NotNull String article) {

        log.debug("order {} for {}", article, userId);
        int cost = cost(article);
        deduct(cost, userId);
        String shipmentId = ship(article, userId);
        log.info("ship {} id {} to {}", article, shipmentId, userId);

        return Json.createObjectBuilder()
            .add("shipment-id", shipmentId)
            .add("article", article)
            .add("user", userId)
            .build();
    }

    private int cost(String article) {
        switch (article) {
            case "expensive gadget":
                return 50;
            case "cheap gadget":
                return 5;
            default:
                throw new NotFoundException("unknown article " + article);
        }
    }

    private void deduct(int cost, int userId) {
        int balance = balance(userId);
        if (balance < cost) {
            throw new OutOfCreditException(balance, cost,
                URI.create("/account/12345/msgs/abc"),
                ACCOUNT_1, ACCOUNT_2
            );
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


    public static final URI ACCOUNT_1 = URI.create("/account/12345");
    public static final URI ACCOUNT_2 = URI.create("/account/67890");
}
