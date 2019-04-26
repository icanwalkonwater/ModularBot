package com.jesus_crie.modularbot.core.dependencyinjection.exception;

import javax.annotation.Nonnull;

public class InjectionFailedException extends DependencyInjectionException {

    public InjectionFailedException(@Nonnull final Throwable cause) {
        super("Injection failed due to " + cause.getMessage());
        this.initCause(cause);
    }
}
