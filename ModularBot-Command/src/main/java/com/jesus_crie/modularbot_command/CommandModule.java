package com.jesus_crie.modularbot_command;

import com.jesus_crie.modularbot.ModularBotBuilder;
import com.jesus_crie.modularbot.module.BaseModule;
import com.jesus_crie.modularbot_command.annotations.CommandInfo;
import com.jesus_crie.modularbot_command.listener.CommandListener;
import com.jesus_crie.modularbot_command.listener.DiscordCommandListener;
import com.jesus_crie.modularbot_command.listener.NopCommandListener;
import com.jesus_crie.modularbot_command.processing.CommandProcessor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public class CommandModule extends BaseModule {

    private static final ModuleInfo INFO = new ModuleInfo("Command", "Jesus-Crie",
            "https://github.com/JesusCrie/ModularBot", "1.0", 1);

    // Prefix stuff
    private String defaultPrefix = "!";
    private Map<Long, String> customPrefix = new HashMap<>();

    // Command storing
    private List<Command> commandStorage = new ArrayList<>();

    // Command processor
    private CommandProcessor processor = new CommandProcessor();

    private List<CommandListener> listeners = new ArrayList<>();

    public CommandModule() {
        super(INFO);
    }

    @Override
    public void onLoad(@Nonnull final ModularBotBuilder builder) {
        builder.addListeners(new DiscordCommandListener(this));
    }

    public void registerCommands(@Nonnull final Command... commands) {
        Collections.addAll(commandStorage, commands);
    }

    public void registerQuickCommand(@Nonnull final String name, @Nonnull final Consumer<CommandEvent> action) {
        registerCommands(new QuickCommand(name, AccessLevel.EVERYONE, action));
    }

    public void registerCreatorQuickCommand(@Nonnull final String name, @Nonnull final Consumer<CommandEvent> action) {
        registerCommands(new QuickCommand(name, AccessLevel.CREATOR, action));
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

    /**
     * Set the id of the owner of the bot used in {@link AccessLevel#CREATOR}.
     *
     * @param owner The discord id of the account of the creator.
     */
    public void setOwnerId(final long owner) {
        AccessLevel.CREATOR_ID = owner;
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

    public void addListener(@Nonnull final CommandListener listener) {
        listeners.add(listener);
    }

    public void removeListener(@Nonnull final CommandListener listener) {
        listeners.remove(listener);
    }

    public void triggerListeners(@Nonnull final Consumer<CommandListener> action) {
        listeners.forEach(action);
    }
}
