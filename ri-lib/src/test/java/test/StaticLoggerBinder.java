package test;

import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;

public class StaticLoggerBinder implements SLF4JServiceProvider {
    private final MockLoggerFactory factory = new MockLoggerFactory();

    @Override public String getRequestedApiVersion() {return "2.0";}

    @Override public ILoggerFactory getLoggerFactory() { return factory; }

    @Override public IMarkerFactory getMarkerFactory() {return null;}

    @Override public MDCAdapter getMDCAdapter() {return null;}

    @Override public void initialize() {}
}
