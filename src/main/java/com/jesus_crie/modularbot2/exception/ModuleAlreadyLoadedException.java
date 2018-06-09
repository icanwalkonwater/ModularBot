package com.jesus_crie.modularbot2.exception;

public class ModuleAlreadyLoadedException extends RuntimeException {

    public ModuleAlreadyLoadedException(String message) {
        super(message);
    }
}
