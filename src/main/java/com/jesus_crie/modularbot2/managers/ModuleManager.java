package com.jesus_crie.modularbot2.managers;

import com.jesus_crie.modularbot2.module.BaseModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ModuleManager {

    private final ConcurrentHashMap<BaseModule.ModuleInfo, BaseModule> modules = new ConcurrentHashMap<>();
    private final Logger logger = LoggerFactory.getLogger(ModuleManager.class);

    public void registerModules(@Nonnull final BaseModule... toRegister) {
        for (BaseModule module : toRegister) registerModule(module);

    }

    public void registerModule(@Nonnull final BaseModule module) {
        BaseModule m = modules.get(module.getInfo());
        if (m != null) {
            logger.warn("Found another module with the same name \"" + m.getInfo().getName() + " \" ! Keeping the old one...");
            return;
        }

        modules.put(module.getInfo(), module);
    }

    public void loadModules() {
        for (BaseModule module : modules.values()) module.onLoad();
    }

    public void dispatch(Consumer<BaseModule> dispatcher) {
        modules.forEachValue(100, dispatcher);
    }
}
