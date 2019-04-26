package com.jesus_crie.modularbot.core.dependencyinjection.exception;

import javax.annotation.Nonnull;

public class NoInjectorTargetException extends DependencyInjectionException {

    public NoInjectorTargetException(@Nonnull final String className) {
        super(String.format("The class %s can't be injected, no suitable methods found. Did you forget the @InjectorTarget ?", className));
    }
}
