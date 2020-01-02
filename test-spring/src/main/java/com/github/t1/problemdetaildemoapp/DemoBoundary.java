package com.github.t1.problemdetaildemoapp;

import com.github.t1.problemdetaildemoapp.DemoService.PaymentMethod;
import com.github.t1.problemdetaildemoapp.DemoService.Shipment;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;

@RestController
@RequestMapping(path = "/orders")
@RequiredArgsConstructor
public class DemoBoundary {
    private final DemoService service;

    @PostMapping public Shipment order(
        @RequestParam("user") int userId,
        @RequestParam("article") @NotNull String article,
        @RequestParam(value = "payment-method", defaultValue = "prepaid") PaymentMethod paymentMethod) {
        return service.order(userId, article, paymentMethod);
    }
}
