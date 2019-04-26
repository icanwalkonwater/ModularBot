package com.jesus_crie.modularbot.core.dependencyinjection.exception;

import javax.annotation.Nonnull;

public class TooManyInjectorTargetException extends DependencyInjectionException {

    public TooManyInjectorTargetException(@Nonnull final String className) {
        super(String.format("The class %s has multiple annotated constructors, only one is allowed.", className));
    }
}
