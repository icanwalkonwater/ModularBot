package com.jesus_crie.modularbot2.module.commands;

import com.jesus_crie.modularbot2.module.BaseModule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class CommandModule extends BaseModule {

    private static final ModuleInfo INFO = new ModuleInfo(CommandModule.class, "Command", "Jesus-Crie",
            "https://github.com/JesusCrie/ModularBot", "1.0", 1);

    // Prefix stuff
    private String defaultPrefix = "!";
    private Map<Long, String> customPrefix = new HashMap<>();

    // Command storing
    private List<Command> commandStorage = new ArrayList<>();

    public CommandModule() {
        super(INFO);
    }

    public void registerCommands(@Nonnull Command... commands) {
        Collections.addAll(commandStorage, commands);
    }

    @Nonnull
    public String getPrefixForGuild(long guildId) {
        return customPrefix.getOrDefault(guildId, defaultPrefix);
    }

    @Nullable
    public Command getCommand(@Nonnull String name) {
        return commandStorage.stream()
                .filter(c -> c.getAliases().contains(name))
                .findAny()
                .orElse(null);
    }
}
