package com.jesus_crie.modularbot.core.exception;

public class ModuleAlreadyLoadedException extends RuntimeException {

    public ModuleAlreadyLoadedException(String message) {
        super(message);
    }
}
