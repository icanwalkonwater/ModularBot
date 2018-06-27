package com.jesus_crie.modularbot_nashornsupport;

import com.jesus_crie.modularbot.ModularBot;
import com.jesus_crie.modularbot.ModularBotBuilder;
import com.jesus_crie.modularbot.module.BaseModule;
import com.jesus_crie.modularbot.module.Lifecycle;
import com.jesus_crie.modularbot.module.ModuleManager;
import com.jesus_crie.modularbot_nashornsupport.module.BaseJsModule;
import com.jesus_crie.modularbot_nashornsupport.module.JsModule;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class NashornSupportModule extends BaseModule {

    private static final Logger LOG = LoggerFactory.getLogger("NashornSupportModule");

    private static final ModuleInfo INFO = new ModuleInfo("JS Nashorn Support", "Jesus-Crie",
            "https://github.com/JesusCrie/ModularBot", "1.0", 1);

    private static final String SCRIPT_HEADER = "var BaseJsModule = Java.type('com.jesus_crie.modularbot_nashornsupport.module.BaseJsModule');" +
            "var ModuleInfo = Java.type('com.jesus_crie.modularbot.module.BaseModule.ModuleInfo');" +
            "var baseImports = new JavaImporter(java.lang, java.io, java.util," +
            "org.slf4j, com.jesus_crie.modularbot," +
            "net.dv8tion.jda.core, net.dv8tion.jda.core.entities);";

    private NashornScriptEngineFactory engineFactory = new NashornScriptEngineFactory();
    private final File scriptDirectory;
    private Map<String, JsModule> modules = Collections.emptyMap();

    public NashornSupportModule() {
        this(new File("./scripts/"));
    }

    public NashornSupportModule(@Nonnull final File directory) {
        super(INFO);
        scriptDirectory = directory;

        if (!directory.exists()) {
            if (!directory.mkdirs())
                LOG.warn("Can't create the script directory !");
        } else if (!directory.isDirectory())
            throw new IllegalArgumentException("The provided location isn't a directory !");
    }

    private void loadScripts(@Nonnull final File directory) {
        final File[] scripts = directory.listFiles((dir, name) -> name.endsWith(".js") && name.length() > 3);

        if (scripts == null)
            return;

        for (File file : scripts) {
            if (file.isDirectory()) continue;

            try {
                final NashornScriptEngine engine = (NashornScriptEngine) engineFactory.getScriptEngine();
                engine.eval(SCRIPT_HEADER);
                engine.eval(new FileReader(file));

                final JsModule module = new JsModule(engine, file);
                registerScript(file.getName().substring(0, file.getName().length() - 3), module);

            } catch (FileNotFoundException ignore) { // Can't happen
            } catch (ScriptException e) {
                LOG.warn("Failed to load script: " + file);
            }
        }
    }

    public void registerScript(@Nonnull final String name, @Nonnull final JsModule module) {
        if (modules.size() == 0)
            modules = new HashMap<>();

        modules.compute(name, (key, old) -> {
            if (old == null) return module;

            // Skip the already used suffixes.
            int i = 1;
            while (modules.containsKey(name + "-" + i)) {
                i++;
            }

            modules.put(name + "-" + i, module);
            return old;
        });
    }

    @Nullable
    public JsModule getModuleByName(@Nonnull final String name) {
        return modules.get(name);
    }

    private void dispatchToModules(Consumer<BaseJsModule> action) {
        modules.values().forEach(module -> action.accept(module.getJsModule()));
    }

    @Override
    public void onLoad(@Nonnull ModuleManager moduleManager, @Nonnull ModularBotBuilder builder) {
        loadScripts(scriptDirectory);

        dispatchToModules(m -> m.onLoad(moduleManager, builder));
    }

    @Override
    public void onInitialization() {
        if (modules.size() > 0)
            modules = Collections.unmodifiableMap(modules);

        dispatchToModules(Lifecycle::onInitialization);

        LOG.info(modules.size() + " JS modules initialized !");
    }

    @Override
    public void onPostInitialization() {
        dispatchToModules(Lifecycle::onPostInitialization);
    }

    @Override
    public void onPrepareShards() {
        dispatchToModules(Lifecycle::onPrepareShards);
    }

    @Override
    public void onShardsCreated() {
        dispatchToModules(Lifecycle::onShardsCreated);
    }

    @Override
    public void onShardsReady(@Nonnull ModularBot bot) {
        super.onShardsReady(bot);
        dispatchToModules(m -> m.onShardsReadyDelegate(bot));
    }

    @Override
    public void onShutdownShards() {
        dispatchToModules(Lifecycle::onShutdownShards);
    }

    @Override
    public void onUnload() {
        dispatchToModules(Lifecycle::onUnload);
    }
}
