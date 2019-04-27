package com.jesus_crie.modularbot.graalvm;

import com.jesus_crie.modularbot.core.ModularBotBuildInfo;
import com.jesus_crie.modularbot.core.module.Module;

/**
 * The only purpose of this module is the ModuleInfo.
 */
public class GraalSupportModule extends Module {

    //private static final Logger LOG = LoggerFactory.getLogger("GraalVMSupport");

    private static final ModuleInfo INFO = new ModuleInfo("GraalVM Support", ModularBotBuildInfo.AUTHOR,
            ModularBotBuildInfo.GITHUB_URL + ", https://www.graalvm.org",
            ModularBotBuildInfo.VERSION_NAME, ModularBotBuildInfo.BUILD_NUMBER());

    public GraalSupportModule() {
        super(INFO);
    }
}
