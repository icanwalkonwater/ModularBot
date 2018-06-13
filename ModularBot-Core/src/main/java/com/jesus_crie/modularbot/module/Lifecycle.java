package com.jesus_crie.modularbot.module;


import com.jesus_crie.modularbot.ModularBot;
import com.jesus_crie.modularbot.ModularBotBuilder;

import javax.annotation.Nonnull;

public interface Lifecycle {

    default void onLoad(@Nonnull final ModularBotBuilder builder) {}

    default void onInitialization() {}

    default void onPostInitialization() {}

    default void onPrepareShards() {}

    default void onShardsLoaded() {}

    default void onShardsReady(@Nonnull final ModularBot bot) {}

    default void onShutdownShards() {}

    default void onUnload() {}

    enum State {
        STOPPED,
        LOADED,
        INITIALIZED,
        STARTED
    }
}
