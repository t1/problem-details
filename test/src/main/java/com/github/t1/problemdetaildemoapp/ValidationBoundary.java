package com.github.t1.problemdetaildemoapp;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Positive;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
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
