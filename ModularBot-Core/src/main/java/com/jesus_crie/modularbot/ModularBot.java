package com.jesus_crie.modularbot;

import com.jesus_crie.modularbot.module.Lifecycle;
import com.jesus_crie.modularbot.module.ModuleManager;
import com.jesus_crie.modularbot.utils.IStateProvider;
import com.jesus_crie.modularbot.utils.ModularSessionController;
import com.jesus_crie.modularbot.utils.ModularThreadFactory;
import net.dv8tion.jda.bot.sharding.DefaultShardManager;
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.core.audio.factory.IAudioSendFactory;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntFunction;

public class ModularBot extends DefaultShardManager {

    private static final Logger logger = LoggerFactory.getLogger("ModularBot");

    private final AtomicInteger receivedReady = new AtomicInteger();

    protected final ModuleManager moduleManager;

    /**
     * @param token                     The token
     * @param shardsTotal               The total amount of shards or {@code -1} to retrieve the recommended amount from discord.
     * @param stateProvider             Provide the online status, game and idle of each shard.
     * @param enableVoice               Whether or not Voice should be enabled
     * @param enableBulkDeleteSplitting Whether or not {@link DefaultShardManagerBuilder#setBulkDeleteSplittingEnabled(boolean)
     *                                  bulk delete splitting} should be enabled
     * @param useShutdownNow            Whether the ShardManager should use JDA#shutdown() or not
     * @param maxReconnectDelay         The max reconnect delay (default 900)
     * @param corePoolSize              The core pool size for JDA's internal executor (default 2)
     * @param audioSendFactory          The {@link IAudioSendFactory IAudioSendFactory}
     */
    ModularBot(final String token, final int shardsTotal, final @Nullable IStateProvider stateProvider,
               final boolean enableVoice, final boolean enableBulkDeleteSplitting, final boolean useShutdownNow,
               final int maxReconnectDelay, final int corePoolSize, final @Nullable IAudioSendFactory audioSendFactory,
               final @Nonnull ModuleManager moduleManager, final @Nonnull List<IntFunction<Object>> listenerProviders) {
        super(shardsTotal, null,
                new ModularSessionController(),
                new ArrayList<>(), listenerProviders,
                token, new ModularEventManager(),
                audioSendFactory,
                stateProvider != null ? stateProvider.getGameProvider() : null, stateProvider != null ? stateProvider.getOnlineStatusProvider() : null,
                null, null,
                new ModularThreadFactory("Global", true),
                maxReconnectDelay, corePoolSize,
                enableVoice,
                true, enableBulkDeleteSplitting, true,
                stateProvider != null ? stateProvider.getIdleProvider() : null,
                true, useShutdownNow,
                false, null, true);

        this.moduleManager = moduleManager;
        moduleManager.initialize();

        logger.info("ModularBot initialized !");
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
        if (receivedReady.get() == shardsTotal)
            moduleManager.finalizeInitialization(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ScheduledExecutorService createExecutor(ThreadFactory threadFactory) {
        return Executors.newScheduledThreadPool(corePoolSize, r -> {
            Thread t = threadFactory.newThread(r);
            t.setPriority(Thread.NORM_PRIORITY + 1);
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
        super.shutdown();
        moduleManager.unload();

        logger.info("Bot powered off successfully !");
    }
}
