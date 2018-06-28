package com.jesus_crie.modularbot_message_decorator;

import com.jesus_crie.modularbot.ModularBotBuildInfo;
import com.jesus_crie.modularbot.module.BaseModule;

public class MessageDecoratorModule extends BaseModule {

    private static final ModuleInfo INFO = new ModuleInfo("Message Decorator",
            ModularBotBuildInfo.AUTHOR, ModularBotBuildInfo.GITHUB_URL,
            ModularBotBuildInfo.VERSION_NAME, ModularBotBuildInfo.BUILD_NUMBER());

    public MessageDecoratorModule() {
        super(INFO);
    }
}
