package org.slf4j.impl;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.util.concurrent.ConcurrentHashMap;

public class ModularLoggerFactory implements ILoggerFactory {

    private final ConcurrentHashMap<String, ModularLogger> loggers = new ConcurrentHashMap<>();

    @Override
    @Nonnull
    public Logger getLogger(@Nonnull String name) {
        if (loggers.containsKey(name)) return loggers.get(name);
        return new ModularLogger(name);
    }
}
