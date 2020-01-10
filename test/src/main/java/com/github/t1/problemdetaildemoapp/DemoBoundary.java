package com.github.t1.problemdetaildemoapp;

import com.github.t1.problemdetail.ri.ProblemDetailResponseExceptionMapper;
import com.github.t1.problemdetaildemoapp.DemoService.CreditCardLimitExceeded;
import com.github.t1.problemdetaildemoapp.DemoService.PaymentMethod;
import com.github.t1.problemdetaildemoapp.DemoService.Shipment;
import com.github.t1.problemdetaildemoapp.DemoService.UserNotEntitledToOrderOnAccount;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

@Path("/orders")
public class DemoBoundary {
    @Inject DemoService service;

    @POST public Shipment order(
        @FormParam("user") int userId,
        @FormParam("article") @NotNull String article,
        @FormParam("payment-method") @DefaultValue("prepaid") PaymentMethod paymentMethod) {
        return service.order(userId, article, paymentMethod);
    }

    @Path("/orders")
    public interface OrderApi {
        @POST Shipment order(
            @FormParam("user") int userId,
            @FormParam("article") @NotNull String article,
            @FormParam("payment-method") PaymentMethod paymentMethod)
            throws OutOfCreditException, CreditCardLimitExceeded, UserNotEntitledToOrderOnAccount;
    }

    @Path("/indirect")
    @POST public Shipment indirectOrder(
        @FormParam("user") @DefaultValue("1") int userId,
        @FormParam("article") @NotNull String article,
        @FormParam("payment-method") @DefaultValue("prepaid") PaymentMethod paymentMethod,
        @Context UriInfo uriInfo) {

        OrderApi api = RestClientBuilder.newBuilder()
            .baseUri(uriInfo.getBaseUri())
            .build(OrderApi.class);

        return api.order(userId, article, paymentMethod);
    }
}
