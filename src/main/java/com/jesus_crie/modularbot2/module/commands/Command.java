package com.jesus_crie.modularbot2.module.commands;

import com.jesus_crie.modularbot2.module.commands.processing.CommandPattern;

import java.util.ArrayList;
import java.util.List;

public abstract class Command {

    protected final List<String> aliases = new ArrayList<>();
    protected final List<CommandPattern> patterns = new ArrayList<>();

    public List<String> getAliases() {
        return aliases;
    }
}
