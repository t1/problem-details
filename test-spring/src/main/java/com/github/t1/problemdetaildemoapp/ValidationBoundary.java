package com.github.t1.problemdetaildemoapp;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Positive;
import java.time.LocalDate;

import static com.github.t1.validation.ValidationFailedException.validate;

@RestController
@RequestMapping(path = "/validation")
public class ValidationBoundary {
    @Value
    @NoArgsConstructor(force = true) @AllArgsConstructor
    public static class Address {
        @NotNull String street;
        @Positive int zipCode;
        @NotNull String city;
    }

    @Value
    @NoArgsConstructor(force = true) @AllArgsConstructor
    public static class Person {
        @NotNull String firstName;
        @NotEmpty String lastName;
        @Past LocalDate born;
        @Valid Address[] address;
    }

    @PostMapping("/manual") public void postManual() {
        Person person = new Person(null, "", LocalDate.now().plusDays(3),
            new Address[]{new Address(null, -1, null)});

        validate(person);
    }

    @PostMapping("/annotated") public String postAnnotated(@Valid @RequestBody Person person) {
        return "valid:" + person;
    }
}
