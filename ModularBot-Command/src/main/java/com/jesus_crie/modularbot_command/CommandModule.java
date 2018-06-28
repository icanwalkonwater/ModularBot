package com.jesus_crie.modularbot_command;

import com.jesus_crie.modularbot.ModularBotBuildInfo;
import com.jesus_crie.modularbot.ModularBotBuilder;
import com.jesus_crie.modularbot.module.BaseModule;
import com.jesus_crie.modularbot.module.ModuleManager;
import com.jesus_crie.modularbot_command.listener.CommandListener;
import com.jesus_crie.modularbot_command.listener.DiscordCommandListener;
import com.jesus_crie.modularbot_command.processing.CommandProcessor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public class CommandModule extends BaseModule {

    private static final ModuleInfo INFO = new ModuleInfo("Command",
            ModularBotBuildInfo.AUTHOR, ModularBotBuildInfo.GITHUB_URL,
            ModularBotBuildInfo.VERSION_NAME, ModularBotBuildInfo.BUILD_NUMBER());

    // Prefix stuff
    private String defaultPrefix = "!";
    private Map<Long, String> customPrefix = Collections.emptyMap();

    // Command storing
    private final List<Command> commandStorage = new ArrayList<>();

    // Command processor
    private CommandProcessor processor = new CommandProcessor();

    private List<CommandListener> listeners = new ArrayList<>();

    public CommandModule() {
        super(INFO);
    }

    @Override
    public void onLoad(@Nonnull final ModuleManager moduleManager, @Nonnull final ModularBotBuilder builder) {
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
     * Will be automatically called if you use the config module.
     *
     * @param owner The discord id of the account of the creator.
     */
    public void setCreatorId(final long owner) {
        AccessLevel.CREATOR_ID = owner;
    }

    /**
     * Register a new prefix for a guild and override the old one if present.
     * Used by the config module.
     * <p>
     * You can also delete the prefix by letting the prefix {@code null}.
     *
     * @param guildId The id of the targeted guild.
     * @param prefix  The prefix for this guild.
     */
    public void addCustomPrefixForGuild(final long guildId, @Nullable final String prefix) {
        if (customPrefix.size() == 0)
            customPrefix = new HashMap<>();

        if (prefix == null || prefix.length() == 0 || defaultPrefix.equals(prefix))
            customPrefix.remove(guildId);

        customPrefix.put(guildId, prefix);
    }

    /**
     * Get an unmodifiable view of the custom prefixes.
     * Used by the config module.
     *
     * @return A view of the custom prefixes.
     */
    public Map<Long, String> getCustomPrefixes() {
        return Collections.unmodifiableMap(customPrefix);
    }

    @Nonnull
    public String getPrefixForGuild(final long guildId) {
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
