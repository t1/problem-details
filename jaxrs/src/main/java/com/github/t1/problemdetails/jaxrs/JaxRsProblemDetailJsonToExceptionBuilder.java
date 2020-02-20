package com.github.t1.problemdetails.jaxrs;

import com.github.t1.problemdetails.jaxrs.lib.ProblemDetailExceptionRegistry;
import com.github.t1.problemdetails.jaxrs.lib.ProblemDetailJsonToExceptionBuilder;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Stream;

class JaxRsProblemDetailJsonToExceptionBuilder extends ProblemDetailJsonToExceptionBuilder {
    public JaxRsProblemDetailJsonToExceptionBuilder(InputStream entityStream) { super(entityStream); }

    @Override public Throwable build() {
        registerExceptionsThrownByRestClientMethod();
        if (getType() == null) {
            Throwable jaxRsException = createJaxRsException();
            if (jaxRsException != null) {
                return jaxRsException;
            }
        }
        return super.build();
    }

    /**
     * Some magic to find the MP Rest Client interface which is proxied.
     * We then register all exceptions declared on all methods.
     */
    private void registerExceptionsThrownByRestClientMethod() {
        findRestClientClass()
            // .peek(proxy -> log.debug("found rest client class {}", proxy))
            .flatMap(type -> Stream.of(type.getDeclaredMethods()))
            // .peek(method -> log.debug("found rest client method {}", method))
            .flatMap(this::exceptionTypes)
            // .peek(exceptionType -> log.debug("found rest client exception type {}", exceptionType))
            .forEach(ProblemDetailExceptionRegistry::register);
    }

    private Stream<? extends Class<?>> findRestClientClass() {
        Optional<? extends Class<?>> optional = Stream.of(new RuntimeException("dummy").getStackTrace())
            .filter(this::isRestClientMethodCall)
            .findFirst()
            .flatMap(this::classOf);
        // in JDK 9+ we'd call Optional#stream()
        return optional.isPresent() ? Stream.of(optional.get()) : Stream.empty();
    }

    private boolean isRestClientMethodCall(StackTraceElement element) {
        return element.getClassName().startsWith("com.sun.proxy.$Proxy");
    }

    private Optional<Class<?>> classOf(StackTraceElement element) {
        try {
            return Optional.of(Class.forName(element.getClassName()));
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    private Stream<Class<? extends Throwable>> exceptionTypes(Method method) {
        @SuppressWarnings("unchecked")
        Class<? extends Throwable>[] exceptionTypes = (Class<? extends Throwable>[]) method.getExceptionTypes();
        return Stream.of(exceptionTypes);
    }

    private Throwable createJaxRsException() {
        Class<? extends Throwable> type = ProblemDetailExceptionRegistry.computeFrom(getTypeUri(),
            "javax.ws.rs.", "Exception");
        return (type == null) ? null : newInstance(getDetail(), type);
    }
}
