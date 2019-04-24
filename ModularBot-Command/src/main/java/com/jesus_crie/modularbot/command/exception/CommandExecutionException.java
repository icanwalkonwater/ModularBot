package com.jesus_crie.modularbot.command.exception;

import javax.annotation.Nonnull;

public class CommandExecutionException extends RuntimeException {

    public CommandExecutionException(@Nonnull String message, @Nonnull Throwable cause) {
        super(message, cause);
    }

    public CommandExecutionException(@Nonnull Throwable cause) {
        super(cause);
    }
}
