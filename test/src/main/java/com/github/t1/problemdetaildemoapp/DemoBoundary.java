package com.github.t1.problemdetaildemoapp;

import com.github.t1.problemdetaildemoapp.DemoService.PaymentMethod;
import com.github.t1.problemdetaildemoapp.DemoService.Shipment;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/orders")
public class DemoBoundary {
    @Inject DemoService service;

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
