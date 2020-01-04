package com.github.t1.problemdetail.ri;

import com.github.t1.problemdetail.ri.lib.ProblemDetailExceptionRegistry;
import com.github.t1.problemdetail.ri.lib.ProblemDetailJsonToExceptionBuilder;

import java.io.InputStream;

class JaxRsProblemDetailJsonToExceptionBuilder extends ProblemDetailJsonToExceptionBuilder {
    public JaxRsProblemDetailJsonToExceptionBuilder(InputStream entityStream) { super(entityStream); }

    @Override public Throwable build() {
        if (type == null) {
            Throwable jaxRsException = createJaxRsException();
            if (jaxRsException != null) {
                return jaxRsException;
            }
        }
        return super.build();
    }

    private Throwable createJaxRsException() {
        Class<? extends Throwable> type = ProblemDetailExceptionRegistry.computeFrom(getTypeUri(),
            "javax.ws.rs.", "Exception");
        return (type == null) ? null : newInstance(getDetail(), type);
    }
}
