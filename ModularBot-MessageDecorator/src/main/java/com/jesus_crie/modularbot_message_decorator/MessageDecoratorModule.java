package com.jesus_crie.modularbot_message_decorator;

import com.electronwill.nightconfig.core.file.FileConfig;
import com.jesus_crie.modularbot.ModularBotBuildInfo;
import com.jesus_crie.modularbot.ModularBotBuilder;
import com.jesus_crie.modularbot.module.BaseModule;
import com.jesus_crie.modularbot.module.ModuleManager;
import com.jesus_crie.modularbot_message_decorator.decorator.MessageDecorator;
import com.jesus_crie.modularbot_night_config_wrapper.NightConfigWrapperModule;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Collections;
import java.util.Map;

public class MessageDecoratorModule extends BaseModule {

    private static final ModuleInfo INFO = new ModuleInfo("Message Decorator",
            ModularBotBuildInfo.AUTHOR, ModularBotBuildInfo.GITHUB_URL,
            ModularBotBuildInfo.VERSION_NAME, ModularBotBuildInfo.BUILD_NUMBER());

    private static final String CONFIG_DECORATOR_CACHE = "modularDecoratorCache";

    private Map<Long, MessageDecorator<?>> decorators = Collections.emptyMap();
    private final File cacheFile;
    private FileConfig cache;

    public MessageDecoratorModule() {
        this("./decorator_cache.json");
    }

    public MessageDecoratorModule(@Nonnull String cachePath) {
        this(new File(cachePath));
    }

    public MessageDecoratorModule(@Nonnull File cachePath) {
        super(INFO);
        cacheFile = cachePath;
    }

    @Override
    public void onLoad(@Nonnull ModuleManager moduleManager, @Nonnull ModularBotBuilder builder) {
        NightConfigWrapperModule config = moduleManager.getModule(NightConfigWrapperModule.class);
        if (config == null) throw new IllegalStateException("You need to register the module NightConfigWrapperModule prior to this module !");

        config.useSecondaryConfig(CONFIG_DECORATOR_CACHE, cacheFile);
        cache = config.getSecondaryConfig(CONFIG_DECORATOR_CACHE);
    }
}
