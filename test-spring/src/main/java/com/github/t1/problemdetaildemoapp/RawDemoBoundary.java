package com.github.t1.problemdetaildemoapp;

import com.github.t1.problemdetaildemoapp.DemoService.PaymentMethod;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;

@RestController
@RequestMapping(path = "/orders-raw")
@RequiredArgsConstructor
public class RawDemoBoundary {
    private final DemoService service;

    @PostMapping public ResponseEntity<?> order(
        @RequestParam("user") int userId,
        @RequestParam("article") @NotNull String article,
        @RequestParam(value = "payment-method", defaultValue = "prepaid") PaymentMethod paymentMethod) {

        try {
            return ResponseEntity.ok(service.order(userId, article, paymentMethod));
        } catch (OutOfCreditException e) {
            OutOfCreditProblemDetail detail = new OutOfCreditProblemDetail();
            detail.setType(URI.create("https://example.com/probs/out-of-credit"));
            detail.setTitle("You do not have enough credit.");
            detail.setInstance(e.getInstance());
            detail.setBalance(e.getBalance());
            detail.setAccounts(e.getAccounts());
            return ResponseEntity.status(FORBIDDEN).contentType(PROBLEM_DETAIL).body(detail);
        }
    }

    @Data @EqualsAndHashCode(callSuper = true) @ToString(callSuper = true)
    public static class OutOfCreditProblemDetail extends ProblemDetail {
        private int balance;
        private int cost;
        private List<URI> accounts;
    }

    public static final MediaType PROBLEM_DETAIL = MediaType.valueOf("application/problem+json");
}
