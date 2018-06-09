package org.slf4j.impl;

import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

public class StaticLoggerBinder implements LoggerFactoryBinder {

    private static final StaticLoggerBinder SINGLETON = new StaticLoggerBinder();
    public static final String REQUESTED_API_VERSION = "1.7";

    public static StaticLoggerBinder getSingleton() {
        return SINGLETON;
    }

    private ILoggerFactory factory = new ModularLoggerFactory();

    @Override
    public ILoggerFactory getLoggerFactory() {
        return factory;
    }

    /**
     * Override the default factory for another one.
     *
     * @param factory The factory to set.
     */
    public void setLoggerFactory(ILoggerFactory factory) {
        this.factory = factory;
    }

    @Override
    public String getLoggerFactoryClassStr() {
        return factory.getClass().getName();
    }
}
