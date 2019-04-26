package com.jesus_crie.modularbot.core.module;

import com.jesus_crie.modularbot.core.ModularBot;
import com.jesus_crie.modularbot.core.ModularBotBuilder;
import com.jesus_crie.modularbot.core.dependencyinjection.DependencyGraph;
import com.jesus_crie.modularbot.core.dependencyinjection.DependencyInjector;
import com.jesus_crie.modularbot.core.dependencyinjection.exception.DependencyInjectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Handle the modules lifecycle for creation to destruction.
 */
public class ModuleManager {

    private static final Logger LOG = LoggerFactory.getLogger("ModuleManager");

    private DependencyGraph dependencyGraph;
    private final ConcurrentHashMap<Class<? extends Module>, Module> modules = new ConcurrentHashMap<>();
    private boolean initialized = false;

    @Nonnull
    public InjectionContext newContext() {
        if (initialized)
            throw new IllegalStateException("You can't create a new injection context after the manager has been initialized !");
        return new InjectionContext();
    }

    /**
     * Query the dependency graph of the modules, useful for stats.
     * The view returned is unmodifiable.
     *
     * @return An unmodifiable view from the underlying dependency graph.
     */
    public DependencyGraph.View getDependencyGraph() {
        return dependencyGraph.asView();
    }

    /**
     * Query a module.
     *
     * @param clazz - The class of the module.
     * @param <T>   - The type of the module.
     * @return The module.
     * @throws IllegalStateException If the module isn't loaded.
     */
    @SuppressWarnings("unchecked")
    @Nonnull
    public <T extends Module> T getModule(@Nonnull final Class<T> clazz) {
        if (!modules.containsKey(clazz))
            throw new IllegalStateException("This module has not been loaded !");
        return (T) modules.get(clazz);
    }

    /**
     * Trigger the {@link Lifecycle#onLoad(ModuleManager, ModularBotBuilder)} part of the lifecycle of each modules.
     *
     * @param builder - The builder to pass to the modules.
     * @see Lifecycle#onLoad(ModuleManager, ModularBotBuilder)
     */
    public void loadModules(@Nonnull final ModularBotBuilder builder) {
        modules.forEachValue(20, module -> {
            module.onLoad(this, builder);
            module.state = Lifecycle.State.LOADED;
        });
    }

    /**
     * Initialize the module manager and the modules.
     * After that, no context can be created.
     *
     * @see Lifecycle#onInitialization(ModuleManager)
     * @see Lifecycle#onPostInitialization()
     */
    public void initialize() {
        modules.forEachValue(20, module -> module.onInitialization(this));
        initialized = true;

        modules.forEachValue(20, module -> {
            module.onPostInitialization();
            module.state = Lifecycle.State.INITIALIZED;
        });
    }

    /**
     * Finalize the initialisation of the modules when the bot comes online.
     *
     * @param bot - The bot.
     * @see Lifecycle#onShardsReady(ModularBot)
     */
    public void finalizeInitialisation(@Nonnull final ModularBot bot) {
        modules.forEachValue(20, module -> {
            module.onShardsReady(bot);
            module.state = Lifecycle.State.STARTED;
        });
    }

    /**
     * Notify the modules just before the bot goes offline.
     *
     * @see Lifecycle#onShutdownShards()
     */
    public void preUnload() {
        modules.forEachValue(20, module -> {
            module.onShutdownShards();
            module.state = Lifecycle.State.OFFLINE;
        });
    }

    /**
     * Unload the registered modules and free them.
     *
     * @see Lifecycle#onUnload()
     */
    public void unload() {
        modules.forEachValue(20, module -> {
            module.onUnload();
            module.state = Lifecycle.State.STOPPED;
        });

        // Clear the map to allow the GC to collect them
        modules.clear();

        LOG.info("Modules unloaded !");
    }

    /**
     * Hard reset the manage by clearing its module map and resetting its state to uninitialized
     * allowing you to create new contexts.
     */
    public void reset() {
        modules.clear();
        initialized = false;
    }

    /**
     * Dispatch a command to every registered modules.
     *
     * @param action - The action to perform on each module.
     */
    public void dispatch(@Nonnull final Consumer<Module> action) {
        modules.forEachValue(20, action);
    }

    /**
     * A context bound to the manager that provides builder-like methods to
     * manipulate an injector.
     */
    public final class InjectionContext {

        private boolean resolved = false;
        private final DependencyInjector injector = new DependencyInjector();
        private final List<Class<? extends Module>> requests = new LinkedList<>();

        /**
         * Provide some settings destined to a specific module.
         *
         * @param clazz    - The module.
         * @param settings - The settings to provide to the module.
         * @return The current context for chaining.
         */
        @Nonnull
        public InjectionContext provideSettings(@Nonnull final Class<? extends Module> clazz,
                                                @Nonnull final Object... settings) {
            return provideSettings(clazz, new ModuleSettingsProvider(settings));
        }

        /**
         * Provide some settings destined to a specific module.
         *
         * @param clazz    - The module.
         * @param settings - The settings to provide to the module.
         * @return The current context for chaining.
         */
        @Nonnull
        public InjectionContext provideSettings(@Nonnull final Class<? extends Module> clazz,
                                                @Nonnull final ModuleSettingsProvider settings) {
            injector.supplySettings(clazz, settings);
            return this;
        }

        /**
         * Provide some already built modules to the injector.
         *
         * @param modules - The modules to provide.
         * @return The current context for chaining.
         */
        @Nonnull
        public InjectionContext provideBuiltModules(@Nonnull final Module... modules) {
            injector.supplyBuiltModules(modules);
            return this;
        }

        /**
         * Store the given requests and provide them to the injector when resolving the context.
         * These requests are not treated immediately.
         *
         * @param requests - The modules to create.
         * @return The current context for chaining.
         */
        @SafeVarargs
        @Nonnull
        public final InjectionContext requestInjection(@Nonnull final Class<? extends Module>... requests) {
            Collections.addAll(this.requests, requests);
            return this;
        }

        /**
         * @see #requestInjection(Class[])
         */
        public InjectionContext requestInjection(@Nonnull final List<Class<? extends Module>> requests) {
            this.requests.addAll(requests);
            return this;
        }

        /**
         * Check if the context has been resolved or not.
         * @return True if {@link #resolve()} has been called, otherwise false.
         */
        public boolean isResolved() {
            return resolved;
        }

        /**
         * Resolve this context by starting the injector.
         *
         * @throws DependencyInjectionException If the injector throws an error.
         * @throws RuntimeException             If any module throws an error during its construction.
         */
        public void resolve() throws DependencyInjectionException {
            final Collection<Module> ms = injector.resolve(requests);
            modules.clear();
            ms.forEach(module -> modules.put(module.getClass(), module));

            dependencyGraph = injector.getDependencyGraph();
            resolved = true;
        }
    }
}
