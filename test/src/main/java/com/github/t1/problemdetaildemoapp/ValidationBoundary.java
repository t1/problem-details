package com.github.t1.problemdetaildemoapp;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import java.time.LocalDate;

import static com.github.t1.validation.ValidationFailedException.validate;

@Path("/validation")
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
        @NotNull String lastName;
        @Past LocalDate born;
        @Valid Address[] address;
    }

    @POST public void post() {
        Person person = new Person(null, null, LocalDate.now().plusDays(3),
            new Address[]{new Address(null, -1, null)});

        validate(person);
    }
}
