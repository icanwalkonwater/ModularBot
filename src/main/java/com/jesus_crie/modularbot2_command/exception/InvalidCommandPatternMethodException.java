package com.jesus_crie.modularbot2_command.exception;

import javax.annotation.Nonnull;

/**
 * Indicate that the method annotated with {@link com.jesus_crie.modularbot2_command.annotations.RegisterPattern RegisterPattern}
 * is not declared correctly.
 */
public class InvalidCommandPatternMethodException extends RuntimeException {

    public InvalidCommandPatternMethodException(@Nonnull String message) {
        super(message);
    }
}
