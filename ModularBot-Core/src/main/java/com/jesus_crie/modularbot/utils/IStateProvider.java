package com.jesus_crie.modularbot.utils;

import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;

import java.util.function.IntFunction;

public interface IStateProvider {

    IntFunction<Boolean> getIdleProvider();

    IntFunction<Game> getGameProvider();

    IntFunction<OnlineStatus> getOnlineStatusProvider();
}
