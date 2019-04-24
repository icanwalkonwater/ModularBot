package com.jesus_crie.modularbot.command.exception;

public class CommandProcessingException extends Exception {

    private final int cursorPos;
    private final int endPos;

    public CommandProcessingException(String message, int cursorPos, int endPos) {
        super(message);
        this.cursorPos = cursorPos;
        this.endPos = endPos;
    }

    public int getCursorPosition() {
        return cursorPos;
    }

    public int getCursorEndPosition() {
        return endPos;
    }

    @Override
    public String toString() {
        return super.toString() + "[" + cursorPos + "]";
    }
}
