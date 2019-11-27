package com.github.t1.problemdetaildemoapp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import javax.ws.rs.NotFoundException;
import java.net.URI;
import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping(path = "/orders")
public class SpringDemoBoundary {
    @PostMapping
    public String order(
        @RequestParam("user") int userId,
        @RequestParam("article") @NotNull String article) {

        log.info("order {} for {}", article, userId);
        int cost = cost(article);
        deduct(cost, userId);
        String shipmentId = ship(article, userId);
        log.info("ship {} id {} to {}", article, shipmentId, userId);

        return "{" +
            "\"shipment-id\":\"" + shipmentId + "\"," +
            "\"article\":\"" + article + "\"," +
            "\"user\":" + userId + "}";
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
