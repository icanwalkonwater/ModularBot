package com.jesus_crie.modularbot_nashorn_support;

import com.jesus_crie.modularbot.ModularBotBuildInfo;
import com.jesus_crie.modularbot.module.BaseModule;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;

public class NashornSupportModuleNew extends BaseModule {

    private static final ModuleInfo INFO = new ModuleInfo("JS Nashorn Support", ModularBotBuildInfo.AUTHOR,
            ModularBotBuildInfo.GITHUB_URL, ModularBotBuildInfo.VERSION_NAME, ModularBotBuildInfo.BUILD_NUMBER());

    private final NashornScriptEngineFactory FACTORY = new NashornScriptEngineFactory();
    private final File SCRIPT_FOLDER;

    public NashornSupportModuleNew() {
        this(new File("./scripts"));
    }

    public NashornSupportModuleNew(@Nonnull final File scriptLocation) {
        super(INFO);
        if (!scriptLocation.exists()) {
            if (!scriptLocation.mkdirs())
                throw new IllegalStateException("Failed to create script directory !");
        } else if (!scriptLocation.isDirectory())
            throw new IllegalArgumentException("Provided location isn't a directory !");

        SCRIPT_FOLDER = scriptLocation;
    }

    private void loadScripts() {
        final File headerFile = new File(SCRIPT_FOLDER + "__header__.js");
        InputStreamReader headerReader;

        try {
            headerReader = new FileReader(headerFile);
        } catch (FileNotFoundException e) {
            headerReader = new InputStreamReader(getClass().getResourceAsStream("/script_header.js"));
        }
    }

    private NashornScriptEngine createNewEngine() {
        return ((NashornScriptEngine) FACTORY.getScriptEngine());
    }
}
