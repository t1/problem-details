package org.slf4j.impl;

import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;
import test.MockLoggerFactory;

@SuppressWarnings("unused")
public enum StaticLoggerBinder implements LoggerFactoryBinder {
    INSTANCE;

    private MockLoggerFactory factory = new MockLoggerFactory();

    public static StaticLoggerBinder getSingleton() { return INSTANCE; }

    @Override public ILoggerFactory getLoggerFactory() { return factory; }

    @Override public String getLoggerFactoryClassStr() {
        return MockLoggerFactory.class.getName();
    }
}
