package com.jesus_crie.modularbot.graalvm.discordjs;

import com.jesus_crie.modularbot.core.ModularBotBuildInfo;
import com.jesus_crie.modularbot.core.module.Module;

public class GraalSupportDiscordJSModule extends Module {

    private static final ModuleInfo INFO = new ModuleInfo("GraalVM Support Extension - Discord.JS Mocking",
            ModularBotBuildInfo.AUTHOR, ModularBotBuildInfo.GITHUB_URL + ", https://discord.js.org",
            ModularBotBuildInfo.VERSION_NAME, ModularBotBuildInfo.BUILD_NUMBER());

    public GraalSupportDiscordJSModule() {
        super(INFO);
    }
}
