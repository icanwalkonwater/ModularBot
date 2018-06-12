package com.jesus_crie.modularbot2_command;

import com.jesus_crie.modularbot2.ModularBotBuilder;
import com.jesus_crie.modularbot2.module.BaseModule;
import com.jesus_crie.modularbot2_command.listener.CommandListener;
import com.jesus_crie.modularbot2_command.processing.CommandProcessor;

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

    // Command processor
    private CommandProcessor processor = new CommandProcessor();

    public CommandModule() {
        super(INFO);
    }

    @Override
    public void onLoad(@Nonnull ModularBotBuilder builder) {
        builder.addListeners(new CommandListener(this));
    }

    public void registerCommands(@Nonnull Command... commands) {
        Collections.addAll(commandStorage, commands);
    }

    public CommandProcessor getCommandProcessor() {
        return processor;
    }

    public void setCommandProcessorFlags(int... flags) {
        int flag = 0;
        for (int f : flags)
            flag |= f;

        processor = new CommandProcessor(flag);
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
