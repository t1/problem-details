package com.github.t1.problemdetaildemoapp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.problemdetails.Detail;
import org.eclipse.microprofile.problemdetails.Extension;
import org.eclipse.microprofile.problemdetails.Instance;
import org.eclipse.microprofile.problemdetails.Status;
import org.eclipse.microprofile.problemdetails.Title;
import org.eclipse.microprofile.problemdetails.Type;

import java.net.URI;
import java.util.List;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;

@Type("https://example.com/probs/out-of-credit")
@Title("You do not have enough credit.")
@Status(FORBIDDEN)
@Getter @AllArgsConstructor @NoArgsConstructor(force = true)
public class OutOfCreditException extends RuntimeException {
    @Instance private URI instance;
    @Extension private int balance;
    private int cost;
    @Extension private List<URI> accounts;

    @Detail public String getDetail() {
        return "Your current balance is " + balance + ", but that costs " + cost + ".";
    }
}
