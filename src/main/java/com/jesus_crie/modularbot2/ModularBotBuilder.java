package com.jesus_crie.modularbot2;

import com.jesus_crie.modularbot2.module.BaseModule;
import com.jesus_crie.modularbot2.module.ModuleManager;
import com.jesus_crie.modularbot2.module.commands.CommandModule;
import com.jesus_crie.modularbot2.module.logger.ConsoleLoggerModule;
import com.jesus_crie.modularbot2.utils.IStateProvider;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.audio.factory.IAudioSendFactory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;

public class ModularBotBuilder {

    private final String token;
    private int shards = -1;
    private IStateProvider stateProvider = null;
    private boolean enableVoice = false;
    private boolean enableBulkDeleteSplit = true;
    private boolean useShutdownNow = false;
    private int maxReconnectDelay = 900;
    private int poolSize = 2;
    private IAudioSendFactory audioSendFactory = null;
    private final List<IntFunction<Object>> listenersProvider = new ArrayList<>();

    private final ModuleManager moduleManager = new ModuleManager();

    public ModularBotBuilder(@Nonnull final String token) {
        this.token = token;
        moduleManager.autoRegisterModules(this,
                ConsoleLoggerModule.class,
                CommandModule.class);
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
    public ModularBotBuilder setStateProvider(final @Nonnull IStateProvider stateProvider) {
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
    public ModularBotBuilder addListeners(final @Nonnull IntFunction<Object> listenerProvider) {
        listenersProvider.add(listenerProvider);
        return this;
    }

    /**
     * Register some listeners that will be added to all shards.
     * @param listeners The listeners to register.
     */
    public ModularBotBuilder addListeners(final Object... listeners) {
        for (Object listener : listeners) listenersProvider.add(shard -> listener);
        return this;
    }

    /**
     * Register a module through the {@link ModuleManager ModuleManager}.
     *
     * @param module The module to register.
     * @see ModuleManager#registerModule(ModularBotBuilder, BaseModule)
     */
    public ModularBotBuilder registerModule(final @Nonnull BaseModule module) {
        moduleManager.registerModule(this, module);
        return this;
    }

    public ModularBotBuilder registerModules(final BaseModule... modules) {
        moduleManager.registerModules(this, modules);
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
