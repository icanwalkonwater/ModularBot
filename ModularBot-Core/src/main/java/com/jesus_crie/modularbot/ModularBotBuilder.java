package com.jesus_crie.modularbot;

import com.jesus_crie.modularbot.module.BaseModule;
import com.jesus_crie.modularbot.module.ModuleManager;
import com.jesus_crie.modularbot.utils.IStateProvider;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.audio.factory.IAudioSendFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;

public class ModularBotBuilder {

    private static final Logger LOG = LoggerFactory.getLogger("ModularBotBuilder");

    protected final String token;
    protected int shards = -1;
    protected IStateProvider stateProvider = null;
    protected boolean enableVoice = false;
    protected boolean enableBulkDeleteSplit = true;
    protected boolean useShutdownNow = false;
    protected int maxReconnectDelay = 900;
    protected int poolSize = 2;
    protected IAudioSendFactory audioSendFactory = null;
    protected final List<IntFunction<Object>> listenersProvider = new ArrayList<>();

    protected final ModuleManager moduleManager = new ModuleManager();

    public ModularBotBuilder(@Nonnull final String token) {
        this.token = token;
    }

    /**
     * Use a custom amount of shards.
     * (default) -1, Recommended amount of shards.
     *
     * @param shards The amount of shards.
     * @see net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder#setShardsTotal(int)
     */
    public ModularBotBuilder setShardAmount(final int shards) {
        this.shards = shards;
        return this;
    }

    /**
     * Used to provide the state of the bot at startup.
     *
     * @param stateProvider An implementation of {@link IStateProvider IStateProvider}
     * @see net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder#setIdleProvider(IntFunction)
     * @see net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder#setGameProvider(IntFunction)
     * @see net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder#setStatusProvider(IntFunction)
     */
    public ModularBotBuilder setStateProvider(@Nonnull final IStateProvider stateProvider) {
        this.stateProvider = stateProvider;
        return this;
    }

    /**
     * Choice to enable or not the voice. Enable only of you plan to use the audio.
     * (default) false.
     *
     * @param enableVoice Whether or not the audio should be enable.
     * @see net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder#setAudioEnabled(boolean)
     */
    public ModularBotBuilder setEnableVoice(final boolean enableVoice) {
        this.enableVoice = enableVoice;
        return this;
    }

    public ModularBotBuilder useVoice() {
        return setEnableVoice(true);
    }

    /**
     * Split the bulk delete event to individual delete events.
     * (default) true.
     *
     * @param enableBulkDeleteSplit Whether or not the bulk delete events should be split.
     * @see net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder#setBulkDeleteSplittingEnabled(boolean)
     */
    public ModularBotBuilder setEnableBulkDeleteSplit(final boolean enableBulkDeleteSplit) {
        this.enableBulkDeleteSplit = enableBulkDeleteSplit;
        return this;
    }

    public ModularBotBuilder disableBulkDeleteSplitting() {
        return setEnableBulkDeleteSplit(false);
    }

    /**
     * If set to {@code true} the {@link ModularBot#shutdown()} method will use {@link JDA#shutdownNow()}.
     * (default) false.
     *
     * @param useShutdownNow Whether or not to force the shutdown of the shards.
     * @see net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder#setUseShutdownNow(boolean)
     */
    public ModularBotBuilder setUseShutdownNow(final boolean useShutdownNow) {
        this.useShutdownNow = useShutdownNow;
        return this;
    }

    public ModularBotBuilder useShutdownNow() {
        return setUseShutdownNow(true);
    }

    /**
     * Set the maximum amount of time to wait before retrying to reconnect.
     *
     * @param maxReconnectDelay The maximum delay in seconds.
     * @see net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder#setMaxReconnectDelay(int)
     */
    public ModularBotBuilder setMaxReconnectDelay(final int maxReconnectDelay) {
        this.maxReconnectDelay = maxReconnectDelay;
        return this;
    }

    /**
     * Set the amount of threads in the core pool of the bot.
     *
     * @param poolSize The size of the thread pool.
     * @see net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder#setCorePoolSize(int)
     */
    public ModularBotBuilder setPoolSize(final int poolSize) {
        this.poolSize = poolSize;
        return this;
    }

    /**
     * Pre-register listeners for each shards.
     *
     * @param listenerProvider The provider used to create the listener.
     * @see ModularBot#addEventListeners(IntFunction)
     */
    public ModularBotBuilder addListeners(@Nonnull final IntFunction<Object> listenerProvider) {
        listenersProvider.add(listenerProvider);
        return this;
    }

    /**
     * Register some listeners that will be added to all shards.
     * @param listeners The listeners to register.
     */
    public ModularBotBuilder addListeners(@Nonnull final Object... listeners) {
        for (Object listener : listeners) listenersProvider.add(shard -> listener);
        return this;
    }

    /**
     * Register some modules through the {@link ModuleManager ModuleManager}.
     *
     * @param modules The modules to register.
     */
    public ModularBotBuilder registerModules(@Nonnull final BaseModule... modules) {
        moduleManager.registerModules(this, modules);
        return this;
    }

    public ModularBotBuilder registerModules(@Nonnull final Class<? extends BaseModule>[] modulesClass) {
        moduleManager.registerModules(this, modulesClass);
        return this;
    }

    /**
     * Automatically register the modules contained in the base module (logger and command modules).
     */
    @SuppressWarnings("unchecked")
    public ModularBotBuilder autoLoadBaseModules() {

        // Try logger module
        try {
            Class<? extends BaseModule> loggerModule = (Class<? extends BaseModule>) Class.forName("com.jesus_crie.modularbot_logger.ConsoleLoggerModule");
            moduleManager.registerModules(this, loggerModule);
        } catch (ClassNotFoundException e) {
            LOG.debug("Failed to autoload logger module.");
        }

        // Try command module
        try {
            Class<? extends BaseModule> commandModule = (Class<? extends BaseModule>) Class.forName("com.jesus_crie.modularbot_command.CommandModule");
            moduleManager.registerModules(this, commandModule);
        } catch (ClassNotFoundException e) {
            LOG.debug("Failed to autoload command module.");
        }

        // Try night config module
        try {
            Class<? extends BaseModule> nightConfigModule = (Class<? extends BaseModule>) Class.forName("com.jesus_crie.modularbot_nightconfigwrapper.NightConfigWrapperModule");
            moduleManager.registerModules(this, nightConfigModule);
        } catch (ClassNotFoundException e) {
            LOG.debug("Failed to autoload night config module.");
        }

        // Try nashorn module
        try {
            Class<? extends BaseModule> nashornModule = (Class<? extends BaseModule>) Class.forName("com.jesus_crie.modularbot_nashornsupport.NashornSupportModule");
            moduleManager.registerModules(this, nashornModule);
        } catch (ClassNotFoundException e) {
            LOG.debug("Failed to autoload nashorn module.");
        }

        // TODO 16/06/18 remember to complete with new modules

        return this;
    }

    /**
     * Create a new instance of {@link ModularBot ModularBot}.
     *
     * @return A new {@link ModularBot ModularBot}
     */
    public ModularBot build() {
        return new ModularBot(token, shards, stateProvider,
                enableVoice, enableBulkDeleteSplit, useShutdownNow,
                maxReconnectDelay, poolSize, audioSendFactory,
                moduleManager, listenersProvider);
    }
}
