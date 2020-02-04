package com.github.t1.problemdetaildemoapp;

import com.github.t1.problemdetaildemoapp.DemoService.PaymentMethod;
import com.github.t1.problemdetaildemoapp.DemoService.Shipment;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_HTML;

@Path("/orders")
public class DemoBoundary {
    @Inject DemoService service;

    // I hate doing this, but otherwise, e.g. Wildfly replies to a browser with a
    // application/octet-stream, even without a proper MessageBodyWriter => 500 :-/
    @Produces(APPLICATION_JSON)
    @POST public Shipment order(
        @FormParam("user") int userId,
        @FormParam("article") @NotNull String article,
        @FormParam("payment-method") @DefaultValue("prepaid") PaymentMethod paymentMethod) {

        return service.order(userId, article, paymentMethod);
    }

    @Produces(TEXT_HTML)
    @GET public String getForm() {
        return "" +
            "<html><body>\n" +
            "<form method=\"post\" action=\"\"><p/>\n" +
            "  <input type=\"text\" name=\"user\" size=\"50\" value=\"1\"/><br/>\n" +
            "  <input type=\"text\" name=\"article\" size=\"50\" value=\"cheap gadget\"/><br/>\n" +
            "  <input type=\"submit\" value=\"Order\"\n />" +
            "</form>\n" +
            "</body></html>\n";
    }
}
