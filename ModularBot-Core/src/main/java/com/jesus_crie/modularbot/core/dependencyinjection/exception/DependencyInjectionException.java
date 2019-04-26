package com.jesus_crie.modularbot.core.dependencyinjection.exception;

import javax.annotation.Nonnull;

/**
 * General exception for the family of exceptions related to the dependency injector.
 */
public class DependencyInjectionException extends Exception {

    public DependencyInjectionException(@Nonnull final String message) {
        super(message);
    }
}
