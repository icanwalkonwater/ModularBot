package com.jesus_crie.modularbot.exception;

public class ModuleAlreadyLoadedException extends RuntimeException {

    public ModuleAlreadyLoadedException(String message) {
        super(message);
    }
}
