package com.jesus_crie.modularbot;

import com.jesus_crie.modularbot.exception.ModuleAlreadyLoadedException;
import com.jesus_crie.modularbot.module.BaseModule;
import com.jesus_crie.modularbot.module.ModuleManager;
import com.jesus_crie.modularbot.utils.IStateProvider;
import com.jesus_crie.modularbot.utils.ModularSessionController;
import com.jesus_crie.modularbot.utils.ModularThreadFactory;
import net.dv8tion.jda.bot.sharding.ThreadPoolProvider;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.audio.factory.IAudioSendFactory;
import net.dv8tion.jda.core.hooks.IEventManager;
import net.dv8tion.jda.core.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.function.IntFunction;

/**
 * Utility class used to create an instance of {@link ModularBot ModularBot}.
 * This class acts mainly as a simplified version of {@link net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder DefaultShardManagerBuilder}.
 */
public class ModularBotBuilder {

    private static final Logger LOG = LoggerFactory.getLogger("ModularBotBuilder");

    protected final String token;
    protected int shards = -1;
    protected IStateProvider stateProvider = null;
    protected boolean enableVoice = false;
    protected boolean enableBulkDeleteSplitting = true;
    protected boolean useShutdownNow = false;
    protected boolean enableMdcContext = false;
    protected int maxReconnectDelay = 900;
    protected int corePoolSize = 5;
    protected IAudioSendFactory audioSendFactory = null;
    protected final List<IntFunction<Object>> listenersProvider = new ArrayList<>();
    protected IntFunction<? extends ConcurrentMap<String, String>> contextProvider = null;
    protected IntFunction<? extends IEventManager> eventManagerProvider = i -> new ModularEventManager();
    protected ThreadPoolProvider<? extends ScheduledExecutorService> rateLimitPoolProvider = null;
    protected ThreadPoolProvider<? extends ScheduledExecutorService> gatewayPoolProvider = null;
    protected ThreadPoolProvider<? extends ExecutorService> callbackPoolProvider = null;
    protected EnumSet<CacheFlag> cacheFlags = EnumSet.allOf(CacheFlag.class);
    protected ThreadFactory threadFactory = new ModularThreadFactory("General", false);

    protected final ModuleManager moduleManager = new ModuleManager();

    public ModularBotBuilder(@Nonnull final String token) {
        this.token = token;
    }

    /**
     * Use a custom amount of shards.
     * (default) -1, will use the recommended amount of shards.
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

    /**
     * Shortcut for {@code setEnableVoice(true)}.
     */
    public ModularBotBuilder useVoice() {
        return setEnableVoice(true);
    }

    /**
     * Set the {@link IAudioSendFactory IAudioSendFactory} to use.
     *
     * @param audioSendFactory The audio send factory to use.
     */
    public ModularBotBuilder setAudioSendFactory(@Nullable final IAudioSendFactory audioSendFactory) {
        this.audioSendFactory = audioSendFactory;
        return this;
    }

    /**
     * Split the bulk delete event to individual delete events.
     * (default) true.
     *
     * @param enableBulkDeleteSplitting Whether or not the bulk delete events should be split.
     * @see net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder#setBulkDeleteSplittingEnabled(boolean)
     */
    public ModularBotBuilder setEnableBulkDeleteSplit(final boolean enableBulkDeleteSplitting) {
        this.enableBulkDeleteSplitting = enableBulkDeleteSplitting;
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
    public ModularBotBuilder setCorePoolSize(final int poolSize) {
        this.corePoolSize = poolSize;
        return this;
    }

    /**
     * Set the event manager to use instead of {@link ModularEventManager ModularEventManager}.
     *
     * @param eventManagerProvider The event manager provider.
     * @see net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder#setEventManagerProvider(IntFunction)
     */
    public ModularBotBuilder setEventManagerProvider(@Nullable final IntFunction<? extends IEventManager> eventManagerProvider) {
        this.eventManagerProvider = eventManagerProvider;
        return this;
    }

    /**
     * Provide thread pools to use instead of the default ones.
     * USE WITH CAUTION this can alter the behaviour of JDA in many ways !
     *
     * @param rateLimitPoolProvider The thread-pool provider to use for rate-limit handling.
     * @param gatewayPoolProvider   The thread-pool provider to use for main WebSocket workers.
     * @param callbackPoolProvider  The thread-pool provider to use for callback handling.
     * @see net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder#setRateLimitPoolProvider(ThreadPoolProvider)
     * @see net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder#setGatewayPoolProvider(ThreadPoolProvider)
     * @see net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder#setCallbackPoolProvider(ThreadPoolProvider)
     */
    public ModularBotBuilder setCustomThreadPools(
            @Nullable final ThreadPoolProvider<? extends ScheduledExecutorService> rateLimitPoolProvider,
            @Nullable final ThreadPoolProvider<? extends ScheduledExecutorService> gatewayPoolProvider,
            ThreadPoolProvider<? extends ExecutorService> callbackPoolProvider
    ) {
        this.rateLimitPoolProvider = rateLimitPoolProvider;
        this.gatewayPoolProvider = gatewayPoolProvider;
        this.callbackPoolProvider = callbackPoolProvider;
        return this;
    }

    /**
     * Set the parameters related to MDC.
     *
     * @param useContext      True, if JDA should provide an MDC context map.
     * @param contextProvider The provider for <b>modifiable</b> context maps to use in JDA, or {@code null} to reset.
     * @see net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder#setContextEnabled(boolean)
     * @see net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder#setContextMap(IntFunction)
     */
    public ModularBotBuilder setMDCParams(final boolean useContext, @Nullable final IntFunction<? extends ConcurrentMap<String, String>> contextProvider) {
        this.enableMdcContext = useContext;
        this.contextProvider = contextProvider;
        return this;
    }

    /**
     * Specify the cache flags to disable.
     *
     * @param flags The flags to disable.
     * @see net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder#setDisabledCacheFlags(EnumSet)
     * @see net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder#setEnabledCacheFlags(EnumSet)
     */
    public ModularBotBuilder setDisableCacheFlags(@Nullable final EnumSet<CacheFlag> flags) {
        if (flags == null) cacheFlags = EnumSet.allOf(CacheFlag.class);
        else cacheFlags = EnumSet.complementOf(flags);

        return this;
    }

    /**
     * Set the {@link ThreadFactory ThreadFactory} that will be used by the manager to create thread.
     * This will not affect thread created by each shard.
     *
     * @param factory The thread factory to use.
     */
    public ModularBotBuilder setManagerThreadFactory(@Nullable final ThreadFactory factory) {
        this.threadFactory = factory;
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
     *
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

        tryLoadModule("com.jesus_crie.modularbot_logger.ConsoleLoggerModule");
        tryLoadModule("com.jesus_crie.modularbot_command.CommandModule");
        tryLoadModule("com.jesus_crie.modularbot_night_config_wrapper.NightConfigWrapperModule");
        tryLoadModule("com.jesus_crie.modularbot_nashorn_support.NashornSupportModule");
        tryLoadModule("com.jesus_crie.modularbot_nashorn_command_support.NashornCommandSupportModule");
        tryLoadModule("com.jesus_crie.modularbot_message_decorator.MessageDecoratorModule");

        // TODO 16/06/18 remember to complete with new modules

        return this;
    }

    /**
     * Internal method, tries to load the given class as a module with exception catching.
     *
     * @param className The name of the class to load.
     */
    @SuppressWarnings("unchecked")
    private void tryLoadModule(@Nonnull final String className) {
        try {
            Class<? extends BaseModule> module = (Class<? extends BaseModule>) Class.forName(className);
            moduleManager.registerModules(this, module);
        } catch (ClassNotFoundException | ModuleAlreadyLoadedException e) {
            LOG.debug("AUTOLOAD: Failed to load class " + className);
        }
    }

    /**
     * Create a new instance of {@link ModularBot ModularBot}.
     *
     * @return A new {@link ModularBot ModularBot}
     */
    public ModularBot build() {
        return new ModularBot(
                shards, new ModularSessionController(),
                listenersProvider, token, eventManagerProvider,
                audioSendFactory, stateProvider, rateLimitPoolProvider,
                gatewayPoolProvider, callbackPoolProvider, threadFactory,
                maxReconnectDelay, corePoolSize, enableVoice,
                enableBulkDeleteSplitting, useShutdownNow, enableMdcContext, contextProvider,
                cacheFlags, moduleManager
        );
    }
}
