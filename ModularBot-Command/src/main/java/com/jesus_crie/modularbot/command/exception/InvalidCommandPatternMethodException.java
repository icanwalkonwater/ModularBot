package com.jesus_crie.modularbot.command.exception;

import com.jesus_crie.modularbot.command.annotations.RegisterPattern;

import javax.annotation.Nonnull;

/**
 * Indicate that the method annotated with {@link RegisterPattern RegisterPattern}
 * is not declared correctly.
 */
public class InvalidCommandPatternMethodException extends RuntimeException {

    public InvalidCommandPatternMethodException(@Nonnull String message) {
        super(message);
    }
}
