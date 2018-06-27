package com.jesus_crie.modularbot_nashornsupport.module;

import com.jesus_crie.modularbot.ModularBot;
import com.jesus_crie.modularbot.module.BaseModule;

import javax.annotation.Nonnull;

/**
 * Module that provide an empty constructor. This class is supposed to be extended by a script.
 */
public abstract class BaseJsModule extends BaseModule {

    private static final ModuleInfo NULL = new ModuleInfo("", "", "", "1.0", 0);

    public BaseJsModule() {
        super(NULL);
    }

    public final void onShardsReadyDelegate(@Nonnull ModularBot bot) {
        this.bot = bot;
        onShardsReady(bot);
    }
}
