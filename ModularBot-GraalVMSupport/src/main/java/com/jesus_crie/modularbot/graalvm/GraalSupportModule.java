package com.jesus_crie.modularbot.graalvm;

import com.jesus_crie.modularbot.core.ModularBotBuildInfo;
import com.jesus_crie.modularbot.core.dependencyinjection.InjectorTarget;
import com.jesus_crie.modularbot.core.module.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The only purpose of this module is the ModuleInfo.
 */
public class GraalSupportModule extends Module {

    private static final Logger LOG = LoggerFactory.getLogger("GraalVM Support");

    private static final ModuleInfo INFO = new ModuleInfo("GraalVM Support", ModularBotBuildInfo.AUTHOR,
            ModularBotBuildInfo.GITHUB_URL + ", https://www.graalvm.org",
            ModularBotBuildInfo.VERSION_NAME, ModularBotBuildInfo.BUILD_NUMBER());

    @InjectorTarget
    public GraalSupportModule() {
        super(INFO);
        LOG.info("Requested");
    }
}
