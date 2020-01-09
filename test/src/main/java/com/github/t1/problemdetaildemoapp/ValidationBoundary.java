package com.github.t1.problemdetaildemoapp;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Positive;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.time.LocalDate;
import java.util.List;

import static com.github.t1.validation.ValidationFailedException.validate;
import static java.util.Collections.singletonList;

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
        @NotEmpty String lastName;
        @Past LocalDate born;
        @Valid List<Address> address;
    }

    @POST @Path("/manual") public void postManual() {
        Person person = new Person(null, "", LocalDate.now().plusDays(3),
            singletonList(new Address(null, -1, null)));

        validate(person);
    }

    @POST @Path("/annotated") public String postAnnotated(@Valid Person person) {
        return "valid:" + person;
    }
}
