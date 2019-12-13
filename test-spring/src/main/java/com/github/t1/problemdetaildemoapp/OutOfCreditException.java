package com.github.t1.problemdetaildemoapp;

import com.github.t1.problemdetail.Detail;
import com.github.t1.problemdetail.Extension;
import com.github.t1.problemdetail.Instance;
import com.github.t1.problemdetail.Status;
import com.github.t1.problemdetail.Title;
import com.github.t1.problemdetail.Type;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.util.List;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;

@Type("https://example.com/probs/out-of-credit")
@Title("You do not have enough credit.")
@Status(FORBIDDEN)
@Getter @AllArgsConstructor @NoArgsConstructor(force = true)
public class OutOfCreditException extends RuntimeException {
    @Extension private int balance;
    private int cost;
    @Instance private URI instance;
    @Extension private List<URI> accounts;

    @Detail public String getDetail() {
        return "Your current balance is " + balance + ", but that costs " + cost + ".";
    }
}
