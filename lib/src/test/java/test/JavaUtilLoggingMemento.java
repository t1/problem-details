package test;

import lombok.Value;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class JavaUtilLoggingMemento implements Extension, BeforeAllCallback, AfterEachCallback, AfterAllCallback {
    public static final List<Log> LOGS = new ArrayList<>();

    private static final Logger ROOT_LOGGER = Logger.getLogger("");
    private static Level rootLoggerLevelMemento;

    public static @Value class Log {
        String logger;
        Level level;
        Throwable thrown;
        String message;
    }

    @Override public void beforeAll(ExtensionContext context) {
        Logger.getLogger("org.junit.jupiter.engine").setLevel(Level.INFO);

        rootLoggerLevelMemento = ROOT_LOGGER.getLevel();
        ROOT_LOGGER.setLevel(Level.ALL);
        if (!findMementoHandler().isPresent()) {
            ROOT_LOGGER.addHandler(new MementoHandler());
        }
    }

    private Optional<Handler> findMementoHandler() {
        return Stream.of(ROOT_LOGGER.getHandlers())
            .filter(handler -> handler instanceof MementoHandler)
            .findAny();
    }

    @Override public void afterEach(ExtensionContext context) {
        LOGS.clear();
    }

    @Override public void afterAll(ExtensionContext context) {
        ROOT_LOGGER.setLevel(rootLoggerLevelMemento);
        findMementoHandler().ifPresent(ROOT_LOGGER::removeHandler);
    }

    private static class MementoHandler extends Handler {
        @Override public void publish(LogRecord record) {
            Log log = new Log(record.getLoggerName(), record.getLevel(), record.getThrown(), record.getMessage());
            LOGS.add(log);
        }

        @Override public void flush() {}

        @Override public void close() {}
    }
}
