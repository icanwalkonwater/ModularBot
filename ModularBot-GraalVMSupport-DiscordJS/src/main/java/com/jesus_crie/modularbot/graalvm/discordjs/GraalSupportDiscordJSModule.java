package com.jesus_crie.modularbot.graalvm.discordjs;

import com.jesus_crie.modularbot.core.ModularBotBuildInfo;
import com.jesus_crie.modularbot.core.dependencyinjection.InjectorTarget;
import com.jesus_crie.modularbot.core.module.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraalSupportDiscordJSModule extends Module {

    private static final Logger LOG = LoggerFactory.getLogger("GraalVM Support DJS");

    private static final ModuleInfo INFO = new ModuleInfo("GraalVM Support Extension - Discord.JS Mocking",
            ModularBotBuildInfo.AUTHOR, ModularBotBuildInfo.GITHUB_URL + ", https://discord.js.org",
            ModularBotBuildInfo.VERSION_NAME, ModularBotBuildInfo.BUILD_NUMBER());

    @InjectorTarget
    public GraalSupportDiscordJSModule() {
        super(INFO);
        LOG.info("Requested");
    }
}
