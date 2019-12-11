package com.github.t1.problemdetaildemoapp;

import com.github.t1.problemdetail.Detail;
import com.github.t1.problemdetail.Instance;
import com.github.t1.problemdetail.ProblemExtension;
import com.github.t1.problemdetail.Status;
import com.github.t1.problemdetail.Title;
import com.github.t1.problemdetail.Type;

import java.net.URI;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;

@Type("https://example.com/probs/out-of-credit")
@Title("You do not have enough credit.")
@Status(FORBIDDEN)
public class OutOfCreditException extends RuntimeException {
    @ProblemExtension private final int balance;
    private final int cost;
    @Instance private final URI instance;
    @ProblemExtension private final List<String> accounts;

    public OutOfCreditException(int balance, int cost, URI instance, URI... accounts) {
        this.balance = balance;
        this.cost = cost;
        this.instance = instance;
        this.accounts = Stream.of(accounts).map(URI::toString).collect(toList());
    }

    @Detail String detail() {
        return "Your current balance is " + balance + ", but that costs " + cost + ".";
    }
}
