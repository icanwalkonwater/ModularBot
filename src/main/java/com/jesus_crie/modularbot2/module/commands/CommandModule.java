package com.jesus_crie.modularbot2.module.commands;

import com.jesus_crie.modularbot2.ModularBotBuilder;
import com.jesus_crie.modularbot2.module.BaseModule;

import javax.annotation.Nonnull;

public class CommandModule extends BaseModule {

    private static final ModuleInfo INFO = new ModuleInfo(CommandModule.class, "Command", "Jesus-Crie",
            "https://github.com/JesusCrie/ModularBot", "1.0", 1);

    public CommandModule() {
        super(INFO);
    }

    @Override
    public void onLoad(@Nonnull ModularBotBuilder builder) {

    }
}
