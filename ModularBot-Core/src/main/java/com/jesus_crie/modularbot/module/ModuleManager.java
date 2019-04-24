package com.jesus_crie.modularbot.module;

import com.jesus_crie.modularbot.ModularBot;
import com.jesus_crie.modularbot.ModularBotBuilder;
import com.jesus_crie.modularbot.exception.ModuleAlreadyLoadedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ModuleManager {

    private final ConcurrentHashMap<Class<? extends BaseModule>, BaseModule> modules = new ConcurrentHashMap<>();
    private final Logger LOG = LoggerFactory.getLogger(ModuleManager.class.getSimpleName());

    private boolean initialized = false;

    public void registerModule(@Nonnull final ModularBotBuilder builder, @Nonnull final BaseModule module) {
        if (initialized)
            throw new IllegalStateException("You can't register a module after the initialization !");

        BaseModule m = modules.get(module.getClass());
        if (m != null)
            throw new ModuleAlreadyLoadedException("Found another module " + m.getClass() + " !");

        modules.put(module.getClass(), module);
        module.onLoad(this, builder);
        module.state = Lifecycle.State.LOADED;
    }

    public void registerModules(@Nonnull final ModularBotBuilder builder, @Nonnull final BaseModule... toRegister) {
        for (BaseModule module : toRegister) registerModule(builder, module);
    }

    @SafeVarargs
    public final void registerModules(@Nonnull final ModularBotBuilder builder, @Nonnull final Class<? extends BaseModule>... classes) {
        for (Class<? extends BaseModule> clazz : classes) {
            try {
                BaseModule module = clazz.getConstructor().newInstance();
                registerModule(builder, module);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                LOG.warn("Autoload failed for module " + clazz.getName(), e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public <T extends BaseModule> T getModule(@Nonnull final Class<T> moduleClass) {
        final T module = (T) modules.get(moduleClass);
        if (module == null)
            throw new IllegalStateException("This module isn't loaded !");
        return module;
    }

    /**
     * Get a module by its class name, can be useful if a module as an optional dependency and avoid raising exceptions.
     *
     * @param className The name of the module to query.
     * @return The given module.
     */
    @SuppressWarnings("unchecked")
    @Nonnull
    public BaseModule getModuleByClassName(@Nonnull final String className) {
        try {
            final Class<? extends BaseModule> clazz = (Class<? extends BaseModule>) Class.forName(className);
            return getModule(clazz);
        } catch (ClassNotFoundException | ClassCastException e) {
            throw new IllegalStateException("This module doesn't exist or isn't loaded !");
        }
    }

    /**
     * Initialize the modules, after this method is called, no other modules can be loaded.
     */
    public void initialize() {
        modules.forEachValue(20, m -> {
            m.onInitialization(this);
            initialized = true;
            m.onPostInitialization();
            m.state = Lifecycle.State.INITIALIZED;
        });

        LOG.info(modules.size() + " modules initialized !");
    }

    public void finalizeInitialization(final @Nonnull ModularBot bot) {
        modules.forEachValue(20, m -> {
            m.onShardsReady(bot);
            if (m.bot == null)
                throw new IllegalStateException("Error in module " + m.getInfo().getName() + ", #onShardsReady() must call super !");
            m.state = Lifecycle.State.STARTED;
        });
    }

    public void preUnload() {
        modules.forEachValue(20, m -> {
            m.onShutdownShards();
            m.state = Lifecycle.State.OFFLINE;
        });
    }

    public void unload() {
        modules.forEachValue(20, m -> {
            m.onUnload();
            m.state = Lifecycle.State.STOPPED;
        });

        LOG.info("Modules unloaded !");
    }

    public void dispatch(Consumer<BaseModule> dispatcher) {
        modules.forEachValue(20, dispatcher);
    }
}
