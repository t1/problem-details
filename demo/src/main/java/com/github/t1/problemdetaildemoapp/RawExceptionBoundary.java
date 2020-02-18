package com.github.t1.problemdetaildemoapp;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Path("/raw")
public class RawExceptionBoundary {
    @Path("/{status}")
    @POST public Response raw(@PathParam("status") @DefaultValue("400") int status) {
        return Response.status(status).type("application/json").build();
    }
}
