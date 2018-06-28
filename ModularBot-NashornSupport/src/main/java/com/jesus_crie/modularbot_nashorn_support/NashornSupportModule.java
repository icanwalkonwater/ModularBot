package com.jesus_crie.modularbot_nashorn_support;

import com.jesus_crie.modularbot.ModularBot;
import com.jesus_crie.modularbot.ModularBotBuilder;
import com.jesus_crie.modularbot.module.BaseModule;
import com.jesus_crie.modularbot.module.Lifecycle;
import com.jesus_crie.modularbot.module.ModuleManager;
import com.jesus_crie.modularbot_nashorn_support.module.BaseJavaScriptModule;
import com.jesus_crie.modularbot_nashorn_support.module.JavaScriptModule;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.script.CompiledScript;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class NashornSupportModule extends BaseModule {

    private static final Logger LOG = LoggerFactory.getLogger("NashornSupportModule");

    private static final ModuleInfo INFO = new ModuleInfo("JS Nashorn Support", "Jesus-Crie",
            "https://github.com/JesusCrie/ModularBot", "1.0", 1);

    private String SCRIPT_HEADER;

    private NashornScriptEngineFactory engineFactory = new NashornScriptEngineFactory();
    private NashornScriptEngine globalEngine = (NashornScriptEngine) engineFactory.getScriptEngine();

    private final File scriptDirectory;
    private Map<String, JavaScriptModule> modules = Collections.emptyMap();

    public NashornSupportModule() {
        this("./scripts/");
    }

    public NashornSupportModule(@Nonnull final String path) {
        this(new File(path));
    }

    public NashornSupportModule(@Nonnull final File directory) {
        super(INFO);
        scriptDirectory = directory;

        if (!directory.exists()) {
            if (!directory.mkdirs())
                LOG.warn("Can't create the script directory !");
        } else if (!directory.isDirectory())
            throw new IllegalArgumentException("The provided location isn't a directory !");

        // Load the header

        final File header = new File(scriptDirectory + "/_script_header.js");

        if (header.exists()) {
            try {
                SCRIPT_HEADER = String.join("\n",
                        Files.readAllLines(Paths.get(header.toURI()), StandardCharsets.UTF_8));
                return;
            } catch (IOException e) {
                LOG.error("Failed to load custom script header ! Loading the default one...");
            }
        }

        LOG.debug("No header found, using the default one.");
        try {
            SCRIPT_HEADER = String.join("\n",
                    Files.readAllLines(
                            Paths.get(NashornSupportModule.class.getResource("/script_header.js").toURI()),
                            StandardCharsets.UTF_8));

        } catch (URISyntaxException | IOException e) { // Should not happen
            LOG.error("Failed to load script_header.js from the JAR ! No header will be used, expect some errors !");
            SCRIPT_HEADER = "";
        }
    }

    /**
     * Load the scripts from the script directory.
     * Skip the header file.
     * For each script, create a new engine, evaluate the header and the script.
     * Then wrap them in a {@link JavaScriptModule JavaScriptModule} and register it.
     * The name of the register is the name of the file without the extension.
     *
     * @param directory The directory that contains the entry points of the modules to load.
     * @see #registerScript(String, JavaScriptModule)
     */
    private void loadScripts(@Nonnull final File directory) {
        final File[] scripts = directory.listFiles((dir, name) -> !name.equals("_script_header.js") && name.endsWith(".js") && name.length() > 3);

        if (scripts == null)
            return;

        for (File file : scripts) {
            if (file.isDirectory()) continue;

            try {
                final NashornScriptEngine engine = (NashornScriptEngine) engineFactory.getScriptEngine();
                engine.eval(SCRIPT_HEADER);
                engine.eval(new FileReader(file));

                final JavaScriptModule module = new JavaScriptModule(engine, file);
                registerScript(file.getName().substring(0, file.getName().length() - 3), module);

            } catch (FileNotFoundException ignore) { // Can't happen
            } catch (ScriptException e) {
                LOG.error("Failed to load script: " + file, e);
            }
        }
    }

    /**
     * Compile an arbitrary script that can be evaluated whenever you want.
     * Not that all of the arbitrary scripts are loaded with the same engine so the code from the previous scripts is
     * still here.
     *
     * @param script The script to execute.
     * @return A {@link CompiledScript CompiledScript} that can be executed at any time on the global engine.
     * @throws ScriptException If the script fail to compile.
     */
    @Nonnull
    public CompiledScript compileArbitraryScript(@Nonnull final String script) throws ScriptException {
        return globalEngine.compile(script);
    }

    /**
     * Compile an arbitrary script that can be evaluated whenever you want.
     * Not that the script is compiled in a dedicated engine so there is no interference possible with another script.
     *
     * @param script The script to compile.
     * @return A {@link CompiledScript CompiledScript} ready.
     * @throws ScriptException If the script fail to compile.
     */
    @Nonnull
    public CompiledScript compileIsolatedScript(@Nonnull final String script) throws ScriptException {
        return ((NashornScriptEngine) engineFactory.getScriptEngine()).compile(script);
    }

    /**
     * Create a new {@link NashornScriptEngine NashornScriptEngine}.
     *
     * @return A new {@link NashornScriptEngine NashornScriptEngine}.
     */
    @Nonnull
    public NashornScriptEngine newNashornEngine() {
        return (NashornScriptEngine) engineFactory.getScriptEngine();
    }

    /**
     * Register a {@link JavaScriptModule JavaScriptModule} by an arbitrary name.
     * If the name is already taken, add a suffix with the index of the duplicate.
     *
     * @param name   The arbitrary name of the module.
     * @param module The wrapper that holds the module.
     * @see #getModuleByName(String)
     */
    public void registerScript(@Nonnull final String name, @Nonnull final JavaScriptModule module) {
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

    /**
     * Get a module by its arbitrary name. Usually the name of the file without the extension.
     *
     * @param name The name of the module to get.
     * @return The module or {@code null} if no modules exist with this name.
     * @see #registerScript(String, JavaScriptModule)
     */
    @Nullable
    public JavaScriptModule getModuleByName(@Nonnull final String name) {
        return modules.get(name);
    }

    /**
     * Get a view of the JS modules currently registered.
     *
     * @return An immutable view of the JS modules currently registered.
     */
    @Nonnull
    public Collection<JavaScriptModule> getModules() {
        return modules.values();
    }

    private void dispatchToModules(Consumer<BaseJavaScriptModule> action) {
        modules.values().forEach(module -> action.accept(module.getJsModule()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoad(@Nonnull ModuleManager moduleManager, @Nonnull ModularBotBuilder builder) {
        loadScripts(scriptDirectory);

        dispatchToModules(m -> m.onLoad(moduleManager, builder));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onInitialization() {
        if (modules.size() > 0)
            modules = Collections.unmodifiableMap(modules);

        dispatchToModules(Lifecycle::onInitialization);

        LOG.info(modules.size() + " JS modules initialized !");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPostInitialization() {
        dispatchToModules(Lifecycle::onPostInitialization);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPrepareShards() {
        dispatchToModules(Lifecycle::onPrepareShards);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onShardsCreated() {
        dispatchToModules(Lifecycle::onShardsCreated);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onShardsReady(@Nonnull ModularBot bot) {
        super.onShardsReady(bot);
        dispatchToModules(m -> m.onShardsReadyDelegate(bot));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onShutdownShards() {
        dispatchToModules(Lifecycle::onShutdownShards);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onUnload() {
        dispatchToModules(Lifecycle::onUnload);
    }
}
