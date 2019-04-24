package com.jesus_crie.modularbot.module;


import com.jesus_crie.modularbot.ModularBot;
import com.jesus_crie.modularbot.ModularBotBuilder;

import javax.annotation.Nonnull;

public interface Lifecycle {

    /**
     * Called when the module is loaded in the first place, occurs when the module is registered.
     *
     * @param moduleManager The current {@link ModuleManager ModuleManager}
     * @param builder       The {@link ModularBotBuilder ModularBotBuilder} which is associated.
     */
    default void onLoad(@Nonnull final ModuleManager moduleManager, @Nonnull final ModularBotBuilder builder) {}

    /**
     * Called when the instance of {@link ModularBot ModularBot} is almost created and the {@link ModuleManager ModuleManager}
     * is still considered as not initialized (you can still register new modules).
     * @param moduleManager - The current {@link ModuleManager}.
     */
    default void onInitialization(@Nonnull final ModuleManager moduleManager) {}

    /**
     * Right after {@link #onInitialization(ModuleManager)}, when the {@link ModuleManager ModuleManager} is initialized and no other
     * modules can be registered.
     */
    default void onPostInitialization() {}

    /**
     * Called just before the shards are being created. (Just before the login).
     */
    default void onPrepareShards() {}

    /**
     * Called right after the shards have been created. (Just after the login).
     */
    default void onShardsCreated() {}

    /**
     * Called right after all of the shards have received the {@link net.dv8tion.jda.core.events.ReadyEvent ReadyEvent}.
     * If you need to do things the the bot come online, put it here.
     *
     * This method MUST call the {@code super}.
     *
     * @param bot The associated instance of {@link ModularBot ModularBot}.
     */
    default void onShardsReady(@Nonnull final ModularBot bot) {}

    /**
     * Called when the shutdown has been initiated, but the shards are still online.
     *
     * If you need to perform a last action on discord, do it here, otherwise wait for {@link #onUnload()}.
     */
    default void onShutdownShards() {}

    /**
     * Called at the end of the shutdown, or when the module is forced to unload due to an error.
     *
     * If you need to save things or do work locally, do it here.
     */
    default void onUnload() {}

    enum State {
        STOPPED,
        LOADED,
        INITIALIZED,
        OFFLINE,
        STARTED
    }
}
