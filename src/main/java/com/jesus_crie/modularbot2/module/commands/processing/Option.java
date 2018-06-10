package com.jesus_crie.modularbot2.module.commands.processing;

public class Option<T> {

    private final String name;
    private final char shortName;
    private final Argument<T> argument;

    public Option(String name, char shortName) {
        this(name, shortName, null);
    }

    public Option(String name, char shortName, Argument<T> argument) {
        this.name = name;
        this.shortName = shortName;
        this.argument = argument;
    }

    public String getLongName() {
        return name;
    }

    public char getShortName() {
        return shortName;
    }

    public boolean hasArgument() {
        return argument == null;
    }

    public Argument<T> getArgument() {
        return argument;
    }
}
