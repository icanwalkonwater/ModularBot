package com.jesus_crie.modularbot.core.dependencyinjection;

import com.jesus_crie.modularbot.core.dependencyinjection.exception.CircularDependencyException;
import com.jesus_crie.modularbot.core.dependencyinjection.exception.InjectionFailedException;
import com.jesus_crie.modularbot.core.dependencyinjection.exception.NoInjectorTargetException;
import com.jesus_crie.modularbot.core.dependencyinjection.exception.TooManyInjectorTargetException;
import com.jesus_crie.modularbot.core.module.Module;
import com.jesus_crie.modularbot.core.module.ModuleSettingsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

public final class DependencyInjector {

    private static final Logger LOG = LoggerFactory.getLogger("DependencyInjector");

    // Input & Output of the injector
    private final Map<Class<? extends Module>, Module> builtModules = new HashMap<>();
    private final Map<Class<? extends Module>, ModuleSettingsProvider> settings = new HashMap<>();

    // Internal data
    private DependencyGraph dependencyGraph;
    private final Map<Class<? extends Module>, Constructor<? extends Module>> injectorTargets = new HashMap<>();
    private final Deque<Class<? extends Module>> queuedInjections = new LinkedList<>();
    private final Deque<Class<? extends Module>> dependencyHierarchy = new LinkedList<>();

    /**
     * Register initial settings to build the modules.
     * The setting bag applied will be the last to be received.
     *
     * @param clazz    - The target class for these settings.
     * @param settings - The setting bag.
     */
    public void supplySettings(@Nonnull final Class<? extends Module> clazz, @Nonnull final ModuleSettingsProvider settings) {
        this.settings.put(clazz, settings);
    }

    /**
     * Register initial modules, already built.
     *
     * @param modules - Modules to register.
     */
    public void supplyBuiltModules(@Nonnull final Module... modules) {
        for (final Module module : modules) {
            builtModules.put(module.getClass(), module);
        }
    }

    /**
     * Supply a dependency graph to avoid recomputing it if has already been done.
     * The injector only compute this graph to spot circular dependencies, by providing it,
     * it will not be recomputed and it will assume that there are no such dependencies.
     * <p>
     * Keep in mind that this is not a source of truth in the sense that it is not used to actually instantiate
     * the modules, only to spot circular dependencies.
     *
     * @param dependencyGraph - The dependency graph to use.
     */
    public void supplyDependencyGraph(@Nonnull final DependencyGraph dependencyGraph) {
        this.dependencyGraph = dependencyGraph;
    }

    /**
     * Query the dependency graph in use in the injector.
     *
     * @return The dependency graph used by this injector.
     */
    @Nonnull
    public DependencyGraph getDependencyGraph() {
        if (dependencyGraph == null)
            throw new IllegalStateException("The dependency graph hasn't been computed yet !");
        return dependencyGraph;
    }

    @Nonnull
    public Collection<Module> resolve(@Nonnull final Class<? extends Module>... requests)
            throws CircularDependencyException, InjectionFailedException, NoInjectorTargetException, TooManyInjectorTargetException {
        return resolve(Arrays.asList(requests));
    }

    /**
     * Process the dependencies of the given modules, build and inject everything, and then
     * return the full list of Modules that were instantiated.
     *
     * @param requests - The modules to process.
     * @return The list of modules that have been instantiated during the process.
     * @throws CircularDependencyException    If a circular dependency is detected, refer to {@link #exploreDependencies(DependencyGraph, Class)}.
     * @throws InjectionFailedException       If for some reason, an exception was thrown by the module during its instantiation.
     * @throws NoInjectorTargetException      If one of the modules to instantiate doesn't have an annotated constructor nor a default one.
     * @throws TooManyInjectorTargetException If one of the modules to instantiate have multiple annotated constructors.
     * @throws RuntimeException               If an injection target throws an exception on its own.
     */
    public Collection<Module> resolve(@Nonnull final List<Class<? extends Module>> requests)
            throws CircularDependencyException, InjectionFailedException, NoInjectorTargetException, TooManyInjectorTargetException {
        LOG.info(String.format("Starting resolution of %d requests...", requests.size()));
        for (Class<? extends Module> request : requests) {
            LOG.debug("- " + request.getSimpleName());
        }

        // Compute dependencies
        if (dependencyGraph == null) {
            LOG.debug("Computing dependency graph...");
            dependencyGraph = computeDependencyGraph(requests);
        }

        // Cleanup already built deps
        LOG.debug("Cleanup queue...");
        for (final Module value : builtModules.values()) {
            queuedInjections.removeIf(r -> r.equals(value.getClass()));
        }

        // Fill the remaining settings providers by the default ones
        LOG.debug("Filling remaining settings providers...");
        for (Class<? extends Module> request : queuedInjections) {
            if (!settings.containsKey(request))
                settings.put(request, extractDefaultSettingsProvider(request));
        }

        // Build and inject them in the correct order
        LOG.debug("Starting construction...");
        while (!queuedInjections.isEmpty()) {
            final Class<? extends Module> request = queuedInjections.pop();
            LOG.debug("Constructing module " + request.getSimpleName() + "...");
            builtModules.put(request, constructAndInject(request));
        }

        // Process late dependencies
        LOG.debug("Starting late injections...");
        for (final Module module : builtModules.values()) {
            fillLateInjections(module);
        }

        LOG.info(String.format("Successfully injected %d modules", builtModules.size()));
        return builtModules.values();
    }

    /**
     * Compute the dependency graph of the requests.
     * Needed in order to spot circular dependencies.
     * Automatically called by the {@link #resolve(Class[])} method, however this method is independent from most
     * of the injector, it's save to call it.
     *
     * @param requests - The root requests to compute from.
     * @return The computed dependency graph.
     * @throws CircularDependencyException    If a circular dependency is detected, refer to {@link #exploreDependencies(DependencyGraph, Class)}.
     * @throws NoInjectorTargetException      If one of the modules to explore doesn't have an annotated constructor nor a default one.
     * @throws TooManyInjectorTargetException If one of the modules to explore have multiple annotated constructors.
     */
    public DependencyGraph computeDependencyGraph(@Nonnull final List<Class<? extends Module>> requests)
            throws CircularDependencyException, NoInjectorTargetException, TooManyInjectorTargetException {
        final DependencyGraph graph = new DependencyGraph();

        for (Class<? extends Module> request : requests) {
            dependencyHierarchy.clear();
            exploreDependencies(graph, request);
        }

        return graph;
    }

    /**
     * Walk through the dependency graph in depth in order to create a queue of injections that ensure that the dependencies
     * of one module will be built before him.
     *
     * @param graph   - The current dependency graph to fill.
     * @param request - The module to explore and process.
     * @throws CircularDependencyException    If a loop has been detected in the hierarchy.
     * @throws NoInjectorTargetException      If no injector target can be found and therefore the exploration can't go further.
     * @throws TooManyInjectorTargetException If multiple injector target have been found.
     */
    private void exploreDependencies(@Nonnull final DependencyGraph graph, @Nonnull final Class<? extends Module> request)
            throws CircularDependencyException, NoInjectorTargetException, TooManyInjectorTargetException {
        // If the module is already present in the hierarchy, that's called a loop
        if (dependencyHierarchy.contains(request))
            throw new CircularDependencyException(dependencyHierarchy, request);

        // Queue the request for injection & add to the hierarchy graph
        queueInjection(request);
        dependencyHierarchy.push(request);

        // Loop through the dependencies
        for (Class<? extends Module> dependency : extractDependencies(request)) {
            // Update dependency graph
            graph.registerParent(dependency, request);
            graph.registerChild(request, dependency);

            exploreDependencies(graph, dependency);
            // Remove the explored dependency from the stack and proceed to its siblings
            dependencyHierarchy.pop();
        }
    }

    /**
     * Extract the dependencies of this modules regardless of the current state of the injector.
     *
     * @param request - The request to process.
     * @return An array of the dependencies of the request.
     * @throws NoInjectorTargetException      If no injector target can be found on the module.
     * @throws TooManyInjectorTargetException If multiple injector target have been found on the module.
     */
    @SuppressWarnings("unchecked")
    @Nonnull
    private Class<? extends Module>[] extractDependencies(@Nonnull final Class<? extends Module> request)
            throws NoInjectorTargetException, TooManyInjectorTargetException {
        // Find the suitable constructor for the request and store it
        final Constructor<? extends Module> constructor = findInjectorTarget(request);
        injectorTargets.put(request, constructor);

        // Extract what arguments are actually dependencies to inject
        return Arrays.stream(constructor.getParameterTypes())
                .filter(Module.class::isAssignableFrom)
                .toArray(Class[]::new);
    }

    /**
     * Make sure that a module is queued once and each subsequent queueing delete the old request.
     *
     * @param request - The module to inject into the queue.
     */
    private void queueInjection(@Nonnull final Class<? extends Module> request) {
        // If already present, delete the old one
        queuedInjections.removeIf(r -> r.equals(request));

        // Add the request on the top of the stack
        queuedInjections.push(request);
    }

    /**
     * Final step after the dependency resolution, build the dependency.
     * This assume that the dependencies of the request have already been built
     * and that the targeted constructor has already been registered.
     * <p>
     * Don't call this method on your own, it won't work.
     *
     * @param request - The module to build and inject.
     * @return The built module.
     * @throws InjectionFailedException If the injection attempt failed.
     * @throws RuntimeException         Exception caused by the constructor that has been rethrown.
     */
    @Nonnull
    private Module constructAndInject(@Nonnull final Class<? extends Module> request) throws InjectionFailedException {
        final Constructor<? extends Module> constructor = injectorTargets.get(request);

        // Check if default constructor
        if (constructor.getParameterCount() == 0) {
            // Ez
            return instantiate(constructor, null);
        }

        // Will walk through each argument and build the arguments based on that
        // Query the settings to fill the holes
        final Class<?>[] parameterTypes = constructor.getParameterTypes();
        final ModuleSettingsProvider settings = this.settings.getOrDefault(request, ModuleSettingsProvider.EMPTY);

        final Object[] arguments = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            final Class<?> parameterType = parameterTypes[i];

            // Is an injection
            if (Module.class.isAssignableFrom(parameterType)) {
                arguments[i] = builtModules.get(parameterType);
            } else {
                // Fill with some user supplied settings (or null if none left)
                arguments[i] = settings.pop();
            }
        }

        // Instantiate this buddy
        return instantiate(constructor, arguments);
    }

    /**
     * Find the adequate constructor to use to instantiate the given dependency.
     *
     * @param request - The module to analyze.
     * @return A suitable constructor to use for injection.
     * @throws NoInjectorTargetException      If no annotated injectors have been found nor default constructor.
     * @throws TooManyInjectorTargetException If multiple annotated constructors have been found.
     */
    @SuppressWarnings("unchecked")
    @Nonnull
    private Constructor<? extends Module> findInjectorTarget(@Nonnull final Class<? extends Module> request)
            throws NoInjectorTargetException, TooManyInjectorTargetException {
        final Constructor<?>[] cs = Arrays.stream(request.getDeclaredConstructors())
                .filter(c -> c.isAnnotationPresent(InjectorTarget.class))
                .toArray(Constructor<?>[]::new);

        // No constructor with the annotation, check for default constructor
        if (cs.length == 0) {
            try {
                return request.getConstructor();
            } catch (NoSuchMethodException e) {
                throw new NoInjectorTargetException(request.getSimpleName());
            }
        } else if (cs.length != 1) { // Multiple constructors with the annotation, not allowed
            throw new TooManyInjectorTargetException(request.getSimpleName());
        }

        // Make accessible
        if (!cs[0].isAccessible())
            cs[0].setAccessible(true);

        return (Constructor<? extends Module>) cs[0];
    }

    @Nonnull
    private ModuleSettingsProvider extractDefaultSettingsProvider(@Nonnull final Class<? extends Module> request) {
        final Field[] sfs = Arrays.stream(request.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(DefaultInjectionParameters.class))
                .filter(f -> Modifier.isStatic(f.getModifiers()))
                .filter(f -> ModuleSettingsProvider.class.isAssignableFrom(f.getType()))
                .toArray(Field[]::new);

        // If not default can be found, well, don't insist.
        if (sfs.length == 0)
            return ModuleSettingsProvider.EMPTY;
        else if (sfs.length > 1)
            LOG.warn("Multiple default settings fields found in " + request.getSimpleName() + ", taking the first one.");

        // Make accessible
        if (!sfs[0].isAccessible()) sfs[0].setAccessible(true);

        try {
            return (ModuleSettingsProvider) sfs[0].get(null);
        } catch (IllegalAccessException e) {
            LOG.warn("Failed to access default settings field in " + request.getSimpleName() + ", ignoring.");
            return ModuleSettingsProvider.EMPTY;
        }
    }

    /**
     * Fill the annotated field and methods of the module with a built module.
     * It assumes that the module has already been built.
     *
     * @param module - The module to late-inject.
     * @throws InjectionFailedException If an injection fails for some reason.
     * @throws RuntimeException         If an underlying method throws an exception.
     */
    private void fillLateInjections(@Nonnull final Module module) throws InjectionFailedException {
        fillLateInjectionFields(module);
        fillLateInjectionSetters(module);
    }

    /**
     * Process and inject the fields of the module.
     *
     * @param module - The module to be injected.
     * @throws InjectionFailedException If an injection fails for some reason.
     */
    @SuppressWarnings("SuspiciousMethodCalls")
    private void fillLateInjectionFields(@Nonnull final Module module) throws InjectionFailedException {
        final Field[] fields = Arrays.stream(module.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(LateInjectorTarget.class))
                .toArray(Field[]::new);

        for (final Field field : fields) {
            try {
                // Don't really care if the field is of the wrong type
                if (!Module.class.isAssignableFrom(field.getType())) {
                    LOG.warn(String.format("Found a late injection target with a wrong type: %s#%s, ignoring.",
                            module.getClass().getSimpleName(), field.getName())
                    );
                    continue;
                }

                if (!builtModules.containsKey(field.getType())) {
                    LOG.warn(String.format("Late injection: Module %s not available, ignoring.", field.getType().getSimpleName()));
                    continue;
                }

                if (!field.isAccessible()) field.setAccessible(true);

                field.set(module, builtModules.get(field.getType()));
            } catch (IllegalAccessException e) {
                throw new InjectionFailedException(e);
            }
        }
    }

    /**
     * Process and inject the methods of the module.
     *
     * @param module - The module to be injected.
     * @throws InjectionFailedException If an injection fails for some reason.
     * @throws RuntimeException         If the underlying method thrown an exception.
     */
    @SuppressWarnings("SuspiciousMethodCalls")
    private void fillLateInjectionSetters(@Nonnull final Module module) throws InjectionFailedException {
        final Method[] methods = Arrays.stream(module.getClass().getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(LateInjectorTarget.class))
                .toArray(Method[]::new);

        for (final Method method : methods) {
            try {
                // Need to be only modules as argument to fill it
                if (Arrays.stream(method.getParameterTypes()).anyMatch(type -> !Module.class.isAssignableFrom(type))) {
                    LOG.warn(String.format("Found a late injection target with non-injectable parameters: %s#%s(%s), ignoring.",
                            module.getClass().getSimpleName(), method.getName(),
                            Arrays.stream(method.getParameterTypes()).map(Class::getSimpleName).collect(Collectors.joining(", ")))
                    );
                }

                if (!method.isAccessible()) method.setAccessible(true);

                // Build arguments
                final Module[] arguments = Arrays.stream(method.getParameterTypes())
                        .map(key -> {
                            if (!builtModules.containsKey(key))
                                LOG.warn(String.format("Late injection: Module %s not available, ignoring.", key.getSimpleName()));
                            return builtModules.get(key);
                        })
                        .toArray(Module[]::new);
                // Call method
                method.invoke(module, (Object[]) arguments);
            } catch (IllegalAccessException e) {
                throw new InjectionFailedException(e);
            } catch (InvocationTargetException e) {
                // Rethrow exception
                throw new RuntimeException(e.getTargetException());
            }
        }
    }

    /**
     * Instantiate a module with its given constructor and arguments.
     *
     * @param constructor - The constructor to use.
     * @param arguments   - The arguments to pass during the instantiation.
     * @return The built module.
     * @throws InjectionFailedException If the constructor couldn't instantiate the module due to its nature.
     * @throws RuntimeException         If the constructor itself has thrown an exception, which is out of the scope of the injector.
     */
    @Nonnull
    private Module instantiate(@Nonnull final Constructor constructor, @Nullable final Object[] arguments) throws InjectionFailedException {
        try {
            if (arguments == null || arguments.length == 0) {
                return (Module) constructor.newInstance();
            } else {
                return (Module) constructor.newInstance(arguments);
            }
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException e) {
            throw new InjectionFailedException(e);
        } catch (InvocationTargetException e) {
            // It's not the scope of the injector to handle exception from the object itself
            throw new RuntimeException(e.getTargetException());
        }
    }
}
