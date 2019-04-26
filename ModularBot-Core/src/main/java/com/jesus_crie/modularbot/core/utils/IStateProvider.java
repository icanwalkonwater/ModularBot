package com.jesus_crie.modularbot.core.utils;

import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;

import java.util.function.IntFunction;

public interface IStateProvider {

    IntFunction<Boolean> getIdleProvider();

    IntFunction<? extends Game> getGameProvider();

    IntFunction<OnlineStatus> getOnlineStatusProvider();

    static IStateProvider from(final IntFunction<Boolean> idleProvider, final IntFunction<? extends Game> gameProvider,
                final IntFunction<OnlineStatus> statusProvider) {
        return new IStateProvider() {
            @Override
            public IntFunction<Boolean> getIdleProvider() {
                return idleProvider;
            }

            @Override
            public IntFunction<? extends Game> getGameProvider() {
                return gameProvider;
            }

            @Override
            public IntFunction<OnlineStatus> getOnlineStatusProvider() {
                return statusProvider;
            }
        };
    }
}
