package com.jesus_crie.modularbot2;

import com.jesus_crie.modularbot2.managers.ModularEventManager;
import com.jesus_crie.modularbot2.managers.ModuleManager;
import com.jesus_crie.modularbot2.utils.IStateProvider;
import com.jesus_crie.modularbot2.utils.ModularSessionController;
import com.jesus_crie.modularbot2.utils.ModularThreadFactory;
import net.dv8tion.jda.bot.sharding.DefaultShardManager;
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.core.audio.factory.IAudioSendFactory;

import javax.security.auth.login.LoginException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;

public class ModularBot extends DefaultShardManager {

    private static ModularBot INSTANCE;
    private static Logger logger;

    public static ModularBot getInstance() {
        return INSTANCE;
    }

    protected ModuleManager moduleManager;

    /**
     * Creates a new DefaultShardManager instance.
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
    ModularBot(String token, int shardsTotal, IStateProvider stateProvider,
               boolean enableVoice, boolean enableBulkDeleteSplitting, boolean useShutdownNow,
               int maxReconnectDelay, int corePoolSize, IAudioSendFactory audioSendFactory) {
        super(shardsTotal, null,
                new ModularSessionController(),
                null, null,
                token, new ModularEventManager(),
                audioSendFactory,
                stateProvider.getGameProvider(), stateProvider.getOnlineStatusProvider(),
                null, null,
                new ModularThreadFactory("Global", true),
                maxReconnectDelay, corePoolSize,
                enableVoice,
                true, enableBulkDeleteSplitting, true,
                stateProvider.getIdleProvider(),
                true, useShutdownNow,
                false, null);

        INSTANCE = this;

        // TODO 10/03/18 logger stuff
    }

    @Override
    public void login() throws LoginException {
        logger.info("Starting " + shardsTotal + " shards...");
        super.login();
    }

    @Override
    protected ScheduledExecutorService createExecutor(ThreadFactory threadFactory) {
        return Executors.newScheduledThreadPool(corePoolSize, r -> {
            Thread t = threadFactory.newThread(r);
            t.setPriority(Thread.NORM_PRIORITY + 1);
            return t;
        });
    }
}
