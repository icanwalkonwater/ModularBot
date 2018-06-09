package com.jesus_crie.modularbot2.module;

import com.jesus_crie.modularbot2.ModularBot;
import com.jesus_crie.modularbot2.ModularBotBuilder;
import com.jesus_crie.modularbot2.exception.ModuleAlreadyLoadedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ModuleManager {

    private final ConcurrentHashMap<Class<? extends BaseModule>, BaseModule> modules = new ConcurrentHashMap<>();
    private final Logger logger = LoggerFactory.getLogger(ModuleManager.class.getSimpleName());

    public void registerModules(@Nonnull final ModularBotBuilder builder, @Nonnull final BaseModule... toRegister) {
        for (BaseModule module : toRegister) registerModule(builder, module);
    }

    public void registerModule(@Nonnull final ModularBotBuilder builder, @Nonnull final BaseModule module) {
        BaseModule m = modules.get(module.getClass());
        if (m != null)
            throw new ModuleAlreadyLoadedException("Found another module " + m.getClass() + " !");

        modules.put(module.getClass(), module);
        module.onLoad(builder);
        module.state = Lifecycle.State.LOADED;
    }

    @SafeVarargs
    public final void autoRegisterModules(@Nonnull final ModularBotBuilder builder, @Nonnull Class<? extends BaseModule>... classes) {
        for (Class<? extends BaseModule> clazz : classes) {
            try {
                BaseModule module = clazz.newInstance();
                registerModule(builder, module);
            } catch (InstantiationException | IllegalAccessException e) {
                logger.warn("Failed to autoload module " + clazz.getName(), e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T extends BaseModule> T getModule(Class<T> moduleClass) {
        return (T) modules.get(moduleClass);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public BaseModule getModuleByClassName(String className) {
        try {
            Class<? extends BaseModule> clazz = (Class<? extends BaseModule>) Class.forName(className);
            return getModule(clazz);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public void initialize() {
        modules.forEachValue(20, m -> {
            m.onInitialization();
            m.onPostInitialization();
            m.state = Lifecycle.State.INITIALIZED;
        });

        logger.info(modules.size() + " modules initialized !");
    }

    public void finalizeInitialization(final @Nonnull ModularBot bot) {
        modules.forEachValue(20, m -> {
            m.onShardsReady(bot);
            m.state = Lifecycle.State.STARTED;
        });
    }

    public void unload() {
        modules.forEachValue(20, m -> {
            m.onUnload();
            m.state = Lifecycle.State.STOPPED;
        });

        logger.info("Modules unloaded !");
    }

    public void dispatch(Consumer<BaseModule> dispatcher) {
        modules.forEachValue(20, dispatcher);
    }
}
