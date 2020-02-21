package com.github.t1.problemdetaildemoapp;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

@RegisterRestClient
public interface PriceService {
    @Path("/prices/{articleId}")
    @Produces(TEXT_PLAIN)
    @GET int get(@PathParam("articleId") String articleId);
}
