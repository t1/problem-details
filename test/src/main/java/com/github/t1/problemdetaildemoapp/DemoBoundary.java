package com.github.t1.problemdetaildemoapp;

import com.github.t1.problemdetaildemoapp.DemoService.PaymentMethod;
import com.github.t1.problemdetaildemoapp.DemoService.Shipment;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Path("/orders")
public class DemoBoundary {
    @Inject DemoService service;

    @POST public Shipment order(
        @FormParam("user") int userId,
        @FormParam("article") @NotNull String article,
        @FormParam("payment-method") @DefaultValue("prepaid") PaymentMethod paymentMethod) {
        return service.order(userId, article, paymentMethod);
    }
}
