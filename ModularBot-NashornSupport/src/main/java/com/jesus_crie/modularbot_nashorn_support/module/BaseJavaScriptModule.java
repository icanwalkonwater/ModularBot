package com.jesus_crie.modularbot_nashorn_support.module;

import com.jesus_crie.modularbot.ModularBot;
import com.jesus_crie.modularbot.module.BaseModule;

import javax.annotation.Nonnull;

/**
 * Module that provide an empty constructor. This class is supposed to be extended by a script.
 */
public abstract class BaseJavaScriptModule extends BaseModule {

    /**
     * Empty {@link com.jesus_crie.modularbot.module.BaseModule.ModuleInfo ModuleInfo}.
     */
    private static final ModuleInfo NULL = new ModuleInfo("", "", "", "1.0", 0);

    public BaseJavaScriptModule() {
        super(NULL);
    }

    /**
     * This method is triggered instead of {@link #onShardsReady(ModularBot)} for this type of module to avoid having
     * to call the super in the JS script.
     *
     * @param bot The associated instance of {@link ModularBot ModularBot};
     */
    public final void onShardsReadyDelegate(@Nonnull ModularBot bot) {
        this.bot = bot;
        onShardsReady(bot);
    }
}
