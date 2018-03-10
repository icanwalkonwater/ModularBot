package com.jesus_crie.modularbot2;

import com.neovisionaries.ws.client.WebSocketFactory;
import net.dv8tion.jda.bot.sharding.DefaultShardManager;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.audio.factory.IAudioSendFactory;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.hooks.IEventManager;
import net.dv8tion.jda.core.utils.SessionController;
import okhttp3.OkHttpClient;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadFactory;
import java.util.function.IntFunction;

public class ModularBot extends DefaultShardManager {

    protected ModularBot(int shardsTotal, Collection<Integer> shardIds, SessionController controller, List<Object> listeners, List<IntFunction<Object>> listenerProviders, String token, IEventManager eventManager, IAudioSendFactory audioSendFactory, IntFunction<Game> gameProvider, IntFunction<OnlineStatus> statusProvider, OkHttpClient.Builder httpClientBuilder, WebSocketFactory wsFactory, ThreadFactory threadFactory, int maxReconnectDelay, int corePoolSize, boolean enableVoice, boolean enableShutdownHook, boolean enableBulkDeleteSplitting, boolean autoReconnect, IntFunction<Boolean> idleProvider, boolean retryOnTimeout, boolean useShutdownNow, boolean enableMDC, IntFunction<ConcurrentMap<String, String>> contextProvider) {
        super(shardsTotal, shardIds, controller, listeners, listenerProviders, token, eventManager, audioSendFactory, gameProvider, statusProvider, httpClientBuilder, wsFactory, threadFactory, maxReconnectDelay, corePoolSize, enableVoice, enableShutdownHook, enableBulkDeleteSplitting, autoReconnect, idleProvider, retryOnTimeout, useShutdownNow, enableMDC, contextProvider);
    }
}
