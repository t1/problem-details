package test;

import com.github.t1.validation.ValidationFailedException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;
import java.util.HashSet;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.BDDAssertions.entry;
import static org.assertj.core.api.BDDAssertions.then;

class ViolationDetailBehavior {
    @Value
    @NoArgsConstructor(force = true) @AllArgsConstructor
    public static class Person {
        @NotNull String firstName;
        @NotNull String lastName;
        // yes, these constraints are impossible to match, that's the point
        @Past @Future LocalDate born;
    }

    @Test void shouldMapSingleViolation() {
        Person person = new Person("Jane", null, null);

        ValidationFailedException throwable = catchThrowableOfType(() -> ValidationFailedException.validate(FACTORY, person), ValidationFailedException.class);

        then(throwable).hasMessage("1 violations failed on ViolationDetailBehavior.Person(firstName=Jane, lastName=null, born=null)");
        then(throwable.violations()).containsExactly(entry("lastName", singleton("must not be null")));
    }

    @Test void shouldMapTwoViolations() {
        Person person = new Person(null, null, null);

        ValidationFailedException throwable = catchThrowableOfType(() -> ValidationFailedException.validate(FACTORY, person), ValidationFailedException.class);

        then(throwable).hasMessage("2 violations failed on ViolationDetailBehavior.Person(firstName=null, lastName=null, born=null)");
        then(throwable.violations()).containsOnly(
            entry("firstName", singleton("must not be null")),
            entry("lastName", singleton("must not be null")));
    }

    @Test void shouldMapTwoViolationsOnTheSameField() {
        LocalDate now = LocalDate.now();
        Person person = new Person("Jane", "Doe", now);

        ValidationFailedException throwable = catchThrowableOfType(() -> ValidationFailedException.validate(FACTORY, person), ValidationFailedException.class);

        then(throwable).hasMessage("2 violations failed on ViolationDetailBehavior.Person(firstName=Jane, lastName=Doe, born=" + now + ")");
        then(throwable.violations()).containsExactly(
            entry("born", new HashSet<>(asList("must be a past date", "must be a future date"))));
    }

    private static final ValidatorFactory FACTORY = Validation.byDefaultProvider()
        .configure()
        .messageInterpolator(new ParameterMessageInterpolator())
        .buildValidatorFactory();
}
