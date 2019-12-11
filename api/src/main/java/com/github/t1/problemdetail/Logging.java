package com.github.t1.problemdetail;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static com.github.t1.problemdetail.LogLevel.AUTO;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Defines how problem details should be logged.
 * <p>
 * Can be applied to the package level, so all exceptions in the package are configured by default.
 */
@Retention(RUNTIME)
@Target({TYPE,PACKAGE})
public @interface Logging {

    /**
     * The category to log to. Defaults to the fully qualified class name of the exception.
     */
    String to() default "";

    /**
     * The level to log at. Defaults to <code>AUTO</code>, i.e. <code>DEBUG</code> for <code>4xx</code>
     * and <code>ERROR</code> for <code>5xx</code>.
     */
    LogLevel at() default AUTO;
}
