package com.jesus_crie.modularbot2.module.commands.exception;

public class CommandProcessingException extends Exception {

    private int cursorPos;

    public CommandProcessingException(String message, int cursorPos) {
        super(message);
        this.cursorPos = cursorPos;
    }

    public int getCursorPosition() {
        return cursorPos;
    }

    @Override
    public String toString() {
        return super.toString() + "[" + cursorPos + "]";
    }
}
