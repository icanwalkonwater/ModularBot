package com.jesus_crie.modularbot2.utils;

import javax.annotation.Nonnull;
import java.util.concurrent.ThreadFactory;

public class ModularThreadFactory implements ThreadFactory {

    private final String identifier;
    private final boolean daemon;

    public ModularThreadFactory(@Nonnull String identifier, boolean daemon) {
        this.identifier = identifier;
        this.daemon = daemon;
    }

    @Override
    public Thread newThread(@Nonnull Runnable r) {
        final Thread t = new Thread(r);
        t.setName(identifier + " #" + t.getId());
        t.setDaemon(daemon);
        return t;
    }
}
