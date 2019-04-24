package com.jesus_crie.modularbot.graalvm.discordjs;

import com.jesus_crie.modularbot.ModularBotBuildInfo;
import com.jesus_crie.modularbot.module.BaseModule;

public class GraalSupportDiscordJSModule extends BaseModule {

    private static final ModuleInfo INFO = new ModuleInfo("GraalVM Support Extension - Discord.JS Mocking", ModularBotBuildInfo.AUTHOR,
            ModularBotBuildInfo.GITHUB_URL + ", https://discord.js.org", ModularBotBuildInfo.VERSION_NAME, ModularBotBuildInfo.BUILD_NUMBER());

    public GraalSupportDiscordJSModule() {
        super(INFO);
    }
}
