package com.jesus_crie.modularbot.v8support.proxying;

import com.eclipsesource.v8.Releasable;
import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Object;
import com.eclipsesource.v8.V8Value;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Used to manage and create proxies of java objects.
 */
public class ProxyManager implements Releasable {

    private final V8 runtime;
    private final Map<Object, V8Object> proxyCache = new ConcurrentHashMap<>();

    public ProxyManager(final V8 runtime) {
        this.runtime = runtime;
    }

    /**
     * Retrieve a proxy object from the given java object.
     * Will map each public method to a JS function on the returned object.
     * <p>
     * If the {@link ProxyExport} annotation is used, only the annotated methods
     * will be mapped.
     * <p>
     * Without the annotation, every non-vararg public method will be mapped.
     * If there are multiple overloads, an arbitrary method which matches the
     * arguments will be used. Refer to {@link JavaAutoOverloadCombiner}.
     * <p>
     * You can also provide a {@link ProxyRules} object that will tell more about
     * what you want to keep if you are not using the annotation.
     * <p>
     * Subsequent calls will with the same parameter will return the exact same
     * object (that was cached).
     *
     * @param target - The object to process.
     * @param rules  - The rules to apply.
     * @return A proxy object to interface with the target.
     */
    @Nonnull
    public V8Object getOrMakeProxy(@Nullable final Object target, @Nullable final ProxyRules rules) {
        if (target == null)
            return (V8Object) V8.getUndefined();

        return proxyCache.computeIfAbsent(target, obj -> createProxy(obj, rules));
    }

    /**
     * Determine which method to use for the proxy and create it.
     *
     * @param target - The targeted object.
     * @param rules  - The rules to apply.
     * @return A proxy object to interface with the target.
     */
    @Nonnull
    private V8Object createProxy(@Nonnull final Object target, @Nullable final ProxyRules rules) {
        if (target instanceof V8Convertible) {
            final V8Object obj = new V8Object(runtime);
            ((V8Convertible) target).bindToV8Object(obj);

            return obj;

        } else if (target.getClass().isAnnotationPresent(ProxyExport.class)) {
            return createGranularProxy(target);
        } else {
            return createAutoProxy(target, rules);
        }
    }

    /**
     * Create proxy using the {@link ProxyExport} annotation.
     *
     * @param target - The targeted object.
     * @return A proxy object to interface with the target.
     */
    @Nonnull
    private V8Object createGranularProxy(@Nonnull final Object target) {
        final List<Method> methods = Arrays.stream(target.getClass().getMethods())
                .filter(m -> m.isAnnotationPresent(ProxyExport.class))
                .collect(Collectors.toList());

        final V8Object proxy = new V8Object(runtime);

        for (Method method : methods) {
            final ProxyExport annotation = method.getAnnotation(ProxyExport.class);
            final String name = annotation.name().isEmpty() ? method.getName() : annotation.name();

            if (annotation.override()) {
                proxy.registerJavaMethod(target, method.getName(), name, method.getParameterTypes());
            } else {
                if (proxy.getType(name) == V8Value.UNDEFINED) {
                    proxy.registerJavaMethod(target, method.getName(), name, method.getParameterTypes());
                }
            }
        }

        return proxy;
    }

    /**
     * Create proxy using every public methods.
     * Overloads are wrapped in a {@link JavaAutoOverloadCombiner}.
     *
     * @param target - The targeted object.
     * @param rules  - The rules to use while creating the proxy.
     * @return A proxy object to interface with the target.
     */
    @Nonnull
    private V8Object createAutoProxy(@Nonnull final Object target, @Nullable final ProxyRules rules) {
        if (rules == null) {
            return createAutoProxyClassic(target);
        } else {
            return createAutoProxyWithRules(target, rules);
        }
    }

    @Nonnull
    private V8Object createAutoProxyClassic(@Nonnull final Object target) {
        final Method[] methods = target.getClass().getMethods();
        final V8Object proxy = new V8Object(runtime);
        final List<String> overloadMethods = new ArrayList<>();

        for (Method method : methods) {
            final String name = method.getName();

            classicMethodProcess(target, methods, proxy, overloadMethods, method, name);
        }

        return proxy;
    }

    @Nonnull
    private V8Object createAutoProxyWithRules(@Nonnull final Object target, @Nonnull final ProxyRules rules) {
        final Method[] methods = target.getClass().getMethods();
        final V8Object proxy = new V8Object(runtime);
        final List<String> overloadMethods = new ArrayList<>();

        for (Method method : methods) {
            final String name = method.getName();

            // If method is dominant, register it immediately
            if (rules.isDominant(method)) {
                proxy.registerJavaMethod(target, name, name, method.getParameterTypes());

                // Register only if non-dominant methods are allowed
            } else if (!rules.isOnlyDominant()) {
                classicMethodProcess(target, methods, proxy, overloadMethods, method, name);
            }
        }

        return proxy;
    }

    /**
     * Process a single method in the classical way.
     *
     * @param target          - The target object.
     * @param methods         - The methods to map.
     * @param proxy           - The proxy object.
     * @param overloadMethods - The methods to exclude.
     * @param method          - The method to register.
     * @param name            - The name of the method to register as.
     */
    private void classicMethodProcess(@Nonnull final Object target, @Nonnull final Method[] methods,
                                      @Nonnull final V8Object proxy, @Nonnull final List<String> overloadMethods,
                                      @Nonnull final Method method, @Nonnull final String name) {

        if (proxy.getType(name) == V8Value.UNDEFINED) {
            proxy.registerJavaMethod(target, name, name, method.getParameterTypes());
        } else if (!overloadMethods.contains(name)) {
            proxy.registerJavaMethod(new JavaAutoOverloadCombiner(target,
                            Arrays.stream(methods).filter(m -> m.getName().equals(name)).collect(Collectors.toList())),
                    name);
            overloadMethods.add(name);
        }
    }

    @Override
    public void close() {
        proxyCache.values().forEach(V8Value::close);
        proxyCache.clear();
    }

    @Override
    @Deprecated
    public void release() {
        close();
    }
}
