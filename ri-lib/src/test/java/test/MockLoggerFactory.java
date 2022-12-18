package test;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.Mockito.mock;

public class MockLoggerFactory implements ILoggerFactory {
    private static final Map<String, Logger> LOGGERS = new LinkedHashMap<>();

    public static void reset() { LOGGERS.clear(); }

    @Override public Logger getLogger(String name) {
        return LOGGERS.computeIfAbsent(name, this::createLogger);
    }

    private Logger createLogger(String name) {
        return mock(Logger.class, "logger:" + name);
    }

    public static Logger onlyLogger(Class<?> type) {
        return onlyLogger(type.getName());
    }

    public static Logger onlyLogger(String name) {
        then(LOGGERS.keySet()).containsOnly(name);
        Logger logger = LOGGERS.remove(name);
        then(logger).isNotNull();
        return logger;
    }
}
