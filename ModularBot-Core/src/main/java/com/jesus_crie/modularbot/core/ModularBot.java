package com.jesus_crie.modularbot.core;

import com.jesus_crie.modularbot.core.module.Lifecycle;
import com.jesus_crie.modularbot.core.module.ModuleManager;
import com.jesus_crie.modularbot.core.utils.IStateProvider;
import com.neovisionaries.ws.client.WebSocketFactory;
import net.dv8tion.jda.bot.sharding.DefaultShardManager;
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.bot.sharding.ThreadPoolProvider;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.audio.factory.IAudioSendFactory;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.IEventManager;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.utils.SessionController;
import net.dv8tion.jda.core.utils.cache.CacheFlag;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.security.auth.login.LoginException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntFunction;

public class ModularBot extends DefaultShardManager {

    // Note: LOG is already define
    private static final Logger logger = LoggerFactory.getLogger("ModularBot");

    private final AtomicInteger receivedReady = new AtomicInteger();

    protected final ModuleManager moduleManager;
    protected final ScheduledExecutorService mainPool = Executors.newScheduledThreadPool(1, r -> {
        final Thread t = new Thread(r);
        t.setDaemon(true);
        t.setName("Main Pool #" + t.getId());
        return t;
    });

    /**
     * @param shardsTotal               The total amount of shards or {@code -1} to retrieve the recommended amount from discord.
     * @param shardIds                  A {@link Collection Collection} of all shard ids that should be started in the beginning or {@code null}
     *                                  to start all possible shards. This will be ignored if shardsTotal is {@code -1}.
     * @param controller                The {@link SessionController SessionController}
     * @param listeners                 The event listeners for new JDA instances.
     * @param listenerProviders         Providers of event listeners for JDA instances. Each will have the shard id applied to them upon
     *                                  shard creation (including shard restarts) and must return an event listener
     * @param token                     The token
     * @param eventManagerProvider      The event manager provider
     * @param audioSendFactory          The {@link IAudioSendFactory IAudioSendFactory}
     * @param gameProvider              The games used at startup of new JDA instances
     * @param statusProvider            The statusProvider used at startup of new JDA instances
     * @param httpClientBuilder         The {@link OkHttpClient.Builder OkHttpClient.Builder}
     * @param httpClient                The {@link OkHttpClient OkHttpClient}
     * @param rateLimitPoolProvider     Provider for the rate-limit pool
     * @param gatewayPoolProvider       Provider for the main ws pool
     * @param callbackPoolProvider      Provider for the callback pool
     * @param wsFactory                 The {@link WebSocketFactory WebSocketFactory}
     * @param threadFactory             The {@link ThreadFactory ThreadFactory}
     * @param maxReconnectDelay         The max reconnect delay
     * @param corePoolSize              The core pool size for JDA's internal executor
     * @param enableVoice               Whether or not Voice should be enabled
     * @param enableShutdownHook        Whether or not the shutdown hook should be enabled
     * @param enableBulkDeleteSplitting Whether or not {@link DefaultShardManagerBuilder#setBulkDeleteSplittingEnabled(boolean)
     *                                  bulk delete splitting} should be enabled
     * @param autoReconnect             Whether or not auto reconnect should be enabled
     * @param idleProvider              The Function that is used to set a shards idle state
     * @param retryOnTimeout            Whether the Requester should retry when a {@link SocketTimeoutException SocketTimeoutException} occurs.
     * @param useShutdownNow            Whether the ShardManager should use JDA#shutdown() or not
     * @param enableMDC                 Whether MDC should be enabled
     * @param contextProvider           The MDC context provider new JDA instances should use on startup
     * @param cacheFlags                The enabled cache flags
     * @param enableCompression         Enable the compression
     */
    public ModularBot(final int shardsTotal, @Nullable final Collection<Integer> shardIds,
               @Nullable final SessionController controller,
               @Nullable final List<Object> listeners, @Nullable final List<IntFunction<Object>> listenerProviders,
               @Nonnull final String token, @Nullable final IntFunction<? extends IEventManager> eventManagerProvider,
               @Nullable final IAudioSendFactory audioSendFactory, @Nullable final IntFunction<? extends Game> gameProvider,
               @Nullable final IntFunction<OnlineStatus> statusProvider,
               @Nullable final OkHttpClient.Builder httpClientBuilder, @Nullable final OkHttpClient httpClient,
               @Nullable final ThreadPoolProvider<? extends ScheduledExecutorService> rateLimitPoolProvider,
               @Nullable final ThreadPoolProvider<? extends ScheduledExecutorService> gatewayPoolProvider,
               @Nullable final ThreadPoolProvider<? extends ExecutorService> callbackPoolProvider,
               @Nullable final WebSocketFactory wsFactory, @Nullable final ThreadFactory threadFactory,
               final int maxReconnectDelay, final int corePoolSize, final boolean enableVoice,
               final boolean enableShutdownHook, final boolean enableBulkDeleteSplitting,
               final boolean autoReconnect, @Nullable final IntFunction<Boolean> idleProvider,
               final boolean retryOnTimeout, final boolean useShutdownNow,
               final boolean enableMDC, @Nullable final IntFunction<? extends ConcurrentMap<String, String>> contextProvider,
               @Nullable final EnumSet<CacheFlag> cacheFlags, final boolean enableCompression,
               @Nonnull final ModuleManager moduleManager) {

        super(shardsTotal, shardIds, controller,
                listeners, listenerProviders,
                token, eventManagerProvider,
                audioSendFactory, gameProvider, statusProvider,
                httpClientBuilder, httpClient,
                rateLimitPoolProvider, gatewayPoolProvider, callbackPoolProvider,
                wsFactory, threadFactory,
                maxReconnectDelay, corePoolSize, enableVoice, enableShutdownHook, enableBulkDeleteSplitting,
                autoReconnect, idleProvider, retryOnTimeout, useShutdownNow,
                enableMDC, contextProvider,
                cacheFlags, enableCompression);

        this.moduleManager = moduleManager;
        moduleManager.initialize();

        logger.info("ModularBot initialized !");
    }

    /**
     * A constructor with some defaults.
     *
     * @param shardsTotal               The total amount of shards or {@code -1} to retrieve the recommended amount from discord.
     * @param controller                The {@link SessionController SessionController}.
     * @param listenerProviders         Providers of event listeners for JDA instances. Each will have the shard id applied to them upon.
     *                                  shard creation (including shard restarts) and must return an event listener.
     * @param token                     The token.
     * @param eventManagerProvider      The event manager provider.
     * @param audioSendFactory          The {@link IAudioSendFactory IAudioSendFactory}.
     * @param stateProvider             The {@link IStateProvider IStateProvider} that wraps the game, status and idle providers.
     * @param rateLimitPoolProvider     Provider for the rate-limit pool.
     * @param gatewayPoolProvider       Provider for the main ws pool.
     * @param callbackPoolProvider      Provider for the callback pool.
     * @param threadFactory             The {@link ThreadFactory ThreadFactory}.
     * @param maxReconnectDelay         The max reconnect delay.
     * @param corePoolSize              The core pool size for JDA's internal executor.
     * @param enableVoice               Whether or not Voice should be enabled.
     * @param enableBulkDeleteSplitting Whether or not {@link DefaultShardManagerBuilder#setBulkDeleteSplittingEnabled(boolean).
     *                                  bulk delete splitting} should be enabled.
     * @param useShutdownNow            Whether the ShardManager should use JDA#shutdown() or not.
     * @param enableMDC                 Whether MDC should be enabled.
     * @param contextProvider           The MDC context provider new JDA instances should use on startup.
     * @param cacheFlags                The enabled cache flags.
     */
    ModularBot(final int shardsTotal, @Nullable final SessionController controller,
               @Nonnull final List<IntFunction<Object>> listenerProviders,
               @Nonnull final String token,
               @Nullable final IntFunction<? extends IEventManager> eventManagerProvider,
               @Nullable final IAudioSendFactory audioSendFactory,
               @Nullable final IStateProvider stateProvider,
               @Nullable final ThreadPoolProvider<? extends ScheduledExecutorService> rateLimitPoolProvider,
               @Nullable final ThreadPoolProvider<? extends ScheduledExecutorService> gatewayPoolProvider,
               @Nullable final ThreadPoolProvider<? extends ExecutorService> callbackPoolProvider,
               @Nullable final ThreadFactory threadFactory,
               final int maxReconnectDelay, final int corePoolSize, final boolean enableVoice,
               final boolean enableBulkDeleteSplitting, final boolean useShutdownNow,
               final boolean enableMDC, @Nullable final IntFunction<? extends ConcurrentMap<String, String>> contextProvider,
               @Nullable final EnumSet<CacheFlag> cacheFlags,
               @Nonnull final ModuleManager moduleManager) {

        this(shardsTotal, null, controller, new ArrayList<>(), listenerProviders, token, eventManagerProvider,
                audioSendFactory, stateProvider == null ? null : stateProvider.getGameProvider(),
                stateProvider == null ? null : stateProvider.getOnlineStatusProvider(),
                null, null, rateLimitPoolProvider, gatewayPoolProvider, callbackPoolProvider,
                null, threadFactory, maxReconnectDelay, corePoolSize, enableVoice, true,
                enableBulkDeleteSplitting, true,
                stateProvider == null ? null : stateProvider.getIdleProvider(), true, useShutdownNow,
                enableMDC, contextProvider, cacheFlags, true, moduleManager);
    }

    /**
     * Get the module manager.
     *
     * @return The {@link ModuleManager} for this instance.
     */
    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    /**
     * Start the bot by connecting it to discord and finalize the initialization of modules.
     *
     * @throws LoginException If the credentials are wrong.
     */
    @Override
    public void login() throws LoginException {
        logger.info("Starting shards...");
        moduleManager.dispatch(Lifecycle::onPrepareShards);

        // Add onReady on every shard
        listeners.add(new ListenerAdapter() {
            @Override
            public void onReady(ReadyEvent event) {
                receivedReady.getAndIncrement();
                ModularBot.this.onReady();
                removeEventListener(this);
            }
        });

        super.login();

        logger.info(shards.size() + " shards successfully spawned !");
        moduleManager.dispatch(Lifecycle::onShardsCreated);
    }

    /**
     * Triggered when a shard is ready.
     */
    private void onReady() {
        if (receivedReady.get() == shardsTotal) {
            logger.info("Shards ready !");
            moduleManager.finalizeInitialisation(this);

            logger.info("Modules initialisation finalized.");
            logger.info("ModularBot successfully started and ready !");
        }
    }

    /**
     * Get a scheduled executor service that can be used without risks.
     * Every thread in this pool is a daemon thread.
     * This thread pool is initialized with a corePoolSize of 1.
     *
     * @return A {@link ScheduledExecutorService ScheduledExecutorService} that can be used to execute tasks.
     */
    public ScheduledExecutorService getMainPool() {
        return mainPool;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ScheduledExecutorService createExecutor(ThreadFactory threadFactory) {
        logger.debug("Creating a new executor.");
        return Executors.newScheduledThreadPool(corePoolSize, r -> {
            Thread t = threadFactory.newThread(r);
            t.setPriority(Thread.NORM_PRIORITY + 1);
            t.setUncaughtExceptionHandler((t1, e) -> logger.error("Uncaught exception !", e));
            return t;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addEventListener(Object... listeners) {
        if (shards != null)
            super.addEventListener(listeners);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addEventListeners(IntFunction<Object> eventListenerProvider) {
        if (shards != null)
            super.addEventListeners(eventListenerProvider);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeEventListener(Object... listeners) {
        if (shards != null)
            super.removeEventListener(listeners);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        logger.info("Shutting down...");
        moduleManager.preUnload();
        receivedReady.set(0);

        if (useShutdownNow) mainPool.shutdownNow();
        else mainPool.shutdown();

        super.shutdown();
        moduleManager.unload();

        logger.info("Bot powered off successfully !");
    }
}
