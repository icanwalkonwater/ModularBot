package com.jesus_crie.modularbot.graalvm;

import com.jesus_crie.modularbot.core.ModularBotBuildInfo;
import com.jesus_crie.modularbot.core.ModularBotBuilder;
import com.jesus_crie.modularbot.core.module.Module;
import com.jesus_crie.modularbot.core.module.ModuleManager;
import org.graalvm.polyglot.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

public class GraalSupportModule extends Module {

    private static final Logger LOG = LoggerFactory.getLogger("GraalVMSupport");

    private static final ModuleInfo INFO = new ModuleInfo("GraalVM Support", ModularBotBuildInfo.AUTHOR,
            ModularBotBuildInfo.GITHUB_URL + ", https://www.graalvm.org",
            ModularBotBuildInfo.VERSION_NAME, ModularBotBuildInfo.BUILD_NUMBER());

    public static Context GLOBAL_CONTEXT = Context.newBuilder().allowAllAccess(true).build();

    public GraalSupportModule() {
        super(INFO);
    }

    @Override
    public void onLoad(@Nonnull final ModuleManager moduleManager, @Nonnull final ModularBotBuilder builder) {

    }
}
