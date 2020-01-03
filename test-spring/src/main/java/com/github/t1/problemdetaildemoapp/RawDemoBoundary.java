package com.github.t1.problemdetaildemoapp;

import com.github.t1.problemdetaildemoapp.DemoService.PaymentMethod;
import com.github.t1.problemdetaildemoapp.DemoService.Shipment;
import com.github.t1.problemdetaildemoapp.DemoService.UserNotEntitledToOrderOnAccount;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.List;

import static com.github.t1.problemdetaildemoapp.DemoService.PROBLEM_INSTANCE;
import static com.github.t1.problemdetaildemoapp.ProblemDetail.JSON_MEDIA_TYPE;
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
            Shipment shipment = service.order(userId, article, paymentMethod);
            return ResponseEntity.ok(shipment);

        } catch (UserNotEntitledToOrderOnAccount e) {
            ProblemDetail detail = new ProblemDetail();
            detail.setType(URI.create("https://api.myshop.example/problems/not-entitled-for-payment-method"));
            detail.setTitle("You're not entitled to use this payment method.");
            detail.setInstance(PROBLEM_INSTANCE);
            return ResponseEntity.status(FORBIDDEN).contentType(ProblemDetail.JSON_MEDIA_TYPE).body(detail);

        } catch (OutOfCreditException e) {
            OutOfCreditProblemDetail detail = new OutOfCreditProblemDetail();
            detail.setType(URI.create("https://example.com/probs/out-of-credit"));
            detail.setTitle("You do not have enough credit.");
            detail.setInstance(e.getInstance());
            detail.setBalance(e.getBalance());
            detail.setAccounts(e.getAccounts());
            return ResponseEntity.status(FORBIDDEN).contentType(ProblemDetail.JSON_MEDIA_TYPE).body(detail);
        }
    }

    @Data @EqualsAndHashCode(callSuper = true) @ToString(callSuper = true)
    public static class OutOfCreditProblemDetail extends ProblemDetail {
        private int balance;
        private int cost;
        private List<URI> accounts;
    }
}
