package com.github.t1.problemdetaildemoapp;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("/prices")
public class PriceBoundary {
    @Path("/{article}")
    @GET public int get(@PathParam("article") String article) {
        switch (article) {
            case "expensive gadget":
                return 50;
            case "cheap gadget":
                return 5;
            default:
                throw new NotFoundException("There is no article [" + article + "]");
        }
    }
}
