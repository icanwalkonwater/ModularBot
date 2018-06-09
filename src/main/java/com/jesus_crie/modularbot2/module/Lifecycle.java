package com.jesus_crie.modularbot2.module;


import com.jesus_crie.modularbot2.ModularBot;
import com.jesus_crie.modularbot2.ModularBotBuilder;

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
