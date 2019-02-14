package com.jesus_crie.modularbot_nashorn_support;

import com.jesus_crie.modularbot.ModularBot;
import com.jesus_crie.modularbot.ModularBotBuildInfo;
import com.jesus_crie.modularbot.ModularBotBuilder;
import com.jesus_crie.modularbot.module.BaseModule;
import com.jesus_crie.modularbot.module.ModuleManager;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.script.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Consumer;

@Deprecated
public class NashornSupportModule extends BaseModule {

    private static final ModuleInfo INFO = new ModuleInfo("JS Nashorn Support", ModularBotBuildInfo.AUTHOR,
            ModularBotBuildInfo.GITHUB_URL, ModularBotBuildInfo.VERSION_NAME, ModularBotBuildInfo.BUILD_NUMBER());

    private static final String DEFAULT_FOLDER = "scripts" + File.separatorChar;
    private static final String SCRIPT_HEADER_FILE = "_header.js";
    private static final String SCRIPT_MAIN_FILE = "main.js";

    private static final Logger LOG = LoggerFactory.getLogger("NashornSupport");

    private final NashornScriptEngine ENGINE;
    private final File SCRIPT_FOLDER;
    private List<JavaScriptModule> modules = new ArrayList<>();

    public NashornSupportModule() {
        this(new File(DEFAULT_FOLDER).getAbsoluteFile());
    }

    public NashornSupportModule(@Nonnull final String scriptLocation) {
        this(new File(scriptLocation));
    }

    public NashornSupportModule(@Nonnull final File scriptLocation) {
        super(INFO);
        if (!scriptLocation.exists()) {
            if (!scriptLocation.mkdirs())
                throw new IllegalStateException("Failed to create script directory !");
        } else if (!scriptLocation.isDirectory())
            throw new IllegalArgumentException("Provided location isn't a directory !");

        SCRIPT_FOLDER = scriptLocation;
        ENGINE = (NashornScriptEngine) new ScriptEngineManager().getEngineByName("js");
    }

    @Nonnull
    public List<JavaScriptModule> getModules() {
        return Collections.unmodifiableList(modules);
    }

    @Nonnull
    public Optional<JavaScriptModule> getModuleByName(@Nonnull final String name) {
        return modules.stream()
                .filter(m -> m.getInfo().getName().equals(name))
                .findAny();
    }

    private void dispatchToModules(@Nonnull final Consumer<JavaScriptModule> action) {
        modules.forEach(action);
    }

    @SuppressWarnings("ConstantConditions")
    private void loadScripts() {
        // Search for file header and load it.
        final File headerFile = new File(SCRIPT_FOLDER + File.separator + SCRIPT_HEADER_FILE);
        InputStreamReader headerReader;

        try {
            headerReader = new FileReader(headerFile);
        } catch (FileNotFoundException e) {
            LOG.warn("No header found, using default header");
            headerReader = new InputStreamReader(getClass().getResourceAsStream("/script_header.js"));
        }

        // Eval the header to create the bindings
        try {
            ENGINE.eval(headerReader);
        } catch (ScriptException e) {
            LOG.error("Failed to evaluate header !", e);
            return;
        }

        final Bindings headerBindings = ENGINE.getBindings(ScriptContext.ENGINE_SCOPE);
        LOG.debug("Script header bindings created");

        // Clear engine bindings
        ENGINE.setBindings(ENGINE.createBindings(), ScriptContext.ENGINE_SCOPE);

        // For each sub folder (considered a module)
        for (final File moduleFolder : SCRIPT_FOLDER.listFiles(File::isDirectory)) {
            final File moduleMain = new File(moduleFolder + File.separator + SCRIPT_MAIN_FILE);

            // If main file doesn't exist
            if (!moduleMain.exists())
                continue;

            try {
                // Eval the main, with copied bindings
                final ScriptContext moduleContext = new SimpleScriptContext();
                moduleContext.setBindings(copyBindings(headerBindings), ScriptContext.ENGINE_SCOPE);
                ENGINE.eval(new FileReader(moduleMain), moduleContext);

                // Register it
                final Object module = ((ScriptObjectMirror) moduleContext.getAttribute("nashorn.global")).getMember("module");
                if (module instanceof ScriptObjectMirror) {
                    final JavaScriptModule jsModule = new JavaScriptModule(((ScriptObjectMirror) module));
                    modules.add(jsModule);

                    LOG.info("Successfully loaded module " + jsModule.getInfo().getName());
                } else {
                    LOG.error("Failed to load module in folder '" + moduleFolder.getName() + "'");
                }

            } catch (ScriptException e) {
                LOG.error("An error happened while evaluating the main file: " + e.getMessage());
            } catch (FileNotFoundException ignore) {
            } // Won't happen we have checked that
        }
    }

    @Nonnull
    private Bindings copyBindings(@Nonnull final Bindings source) {
        final Bindings out = new SimpleBindings();
        out.putAll(source);

        return out;
    }

    @Override
    public void onLoad(@Nonnull final ModuleManager moduleManager, @Nonnull final ModularBotBuilder builder) {
        loadScripts();
        dispatchToModules(m -> m.onLoad(moduleManager, builder));
    }

    @Override
    public void onInitialization() {
        dispatchToModules(JavaScriptModule::onInitialization);
    }

    @Override
    public void onPostInitialization() {
        dispatchToModules(JavaScriptModule::onPostInitialization);
    }

    @Override
    public void onPrepareShards() {
        dispatchToModules(JavaScriptModule::onPrepareShards);
    }

    @Override
    public void onShardsCreated() {
        dispatchToModules(JavaScriptModule::onShardsCreated);
    }

    @Override
    public void onShardsReady(@Nonnull final ModularBot bot) {
        super.onShardsReady(bot);
        dispatchToModules(m -> m.onShardsReady(bot));
    }

    @Override
    public void onShutdownShards() {
        dispatchToModules(JavaScriptModule::onShutdownShards);
    }

    @Override
    public void onUnload() {
        dispatchToModules(JavaScriptModule::onUnload);
    }

}
