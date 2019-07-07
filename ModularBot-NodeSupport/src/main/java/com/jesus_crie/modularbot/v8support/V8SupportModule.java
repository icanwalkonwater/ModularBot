package com.jesus_crie.modularbot.v8support;

import com.eclipsesource.v8.*;
import com.jesus_crie.modularbot.core.ModularBot;
import com.jesus_crie.modularbot.core.ModularBotBuildInfo;
import com.jesus_crie.modularbot.core.dependencyinjection.InjectorTarget;
import com.jesus_crie.modularbot.core.module.Module;
import com.jesus_crie.modularbot.v8support.proxying.JavaAutoOverloadCombiner;
import com.jesus_crie.modularbot.v8support.proxying.ProxyExport;
import com.jesus_crie.modularbot.v8support.proxying.ProxyManager;
import com.jesus_crie.modularbot.v8support.proxying.ProxyRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class V8SupportModule extends Module {

    private static final Logger LOG = LoggerFactory.getLogger("V8Support");

    private static final ModuleInfo INFO = new ModuleInfo("V8/Node.JS Support",
            ModularBotBuildInfo.AUTHOR, ModularBotBuildInfo.GITHUB_URL,
            ModularBotBuildInfo.VERSION_NAME, ModularBotBuildInfo.BUILD_NUMBER());

    private static final String V8_NEW_INSTANCE_HELPER_SCRIPT = "(()=>function(c,a){return new c(...a||[]);})()";

    private final ReentrantLock lock = new ReentrantLock(true);
    private final NodeJS node;
    private final V8 runtime;
    private final V8Function newInstanceFn;
    private final List<V8ModuleWrapper> modules = new ArrayList<>();

    private final ProxyManager proxyManager;

    @InjectorTarget
    public V8SupportModule() {
        super(INFO);
        LOG.info("Requested");
        lock.lock();

        node = NodeJS.createNodeJS();
        runtime = node.getRuntime();
        newInstanceFn = (V8Function) runtime.executeObjectScript(V8_NEW_INSTANCE_HELPER_SCRIPT);

        proxyManager = new ProxyManager(runtime);

        final String nodeVersion = runtime.executeStringScript("process.version");
        LOG.info("Node.JS runtime configured (Node.JS " + nodeVersion + ")");
        releaseLock();
    }

    @Override
    public void onShardsReady(@Nonnull final ModularBot bot) {
        // Manually trigger the caching of the bot object
        getOrMakeProxy(bot);
    }

    @Override
    public void onUnload() {
        acquireLock();

        LOG.info("Releasing " + modules.size() + " modules...");

        // At this point, the modules have already been unloaded by
        // the module manager thanks to the DI
        for (V8ModuleWrapper module : modules) {
            module.close();
        }

        // Releasing cached values
        proxyManager.close();
        newInstanceFn.close();

        node.release();
        lock.unlock();
    }

    /**
     * Get the node runtime.
     * <p>
     * WARNING: The node instance is NOT thread safe.
     * You need to acquire the lock to use it.
     *
     * @return The {@link NodeJS} runtime.
     */
    public NodeJS getNode() {
        return node;
    }

    /**
     * Acquire the lock for the current runtime.
     */
    public void acquireLock() {
        lock.lock();
        runtime.getLocker().acquire();
    }

    /**
     * Release the lock for the current runtime.
     */
    public void releaseLock() {
        runtime.getLocker().release();
        lock.unlock();
    }

    public void registerModule(@Nonnull final V8ModuleWrapper wrapper) {
        modules.add(wrapper);
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
     * <p>
     * This method is thread-safe, the lock will be acquired and then released.
     * Thus if you still need the lock, you need to reacquire it.
     *
     * @param obj   - The object to process.
     * @param rules - The rules to use.
     * @return A proxy object to interface with the target.
     * @see ProxyManager#getOrMakeProxy(Object, ProxyRules)
     */
    @Nonnull
    public V8Object getOrMakeProxy(@Nullable final Object obj, @Nullable final ProxyRules rules) {
        acquireLock();
        final V8Object proxy = proxyManager.getOrMakeProxy(obj, rules);
        releaseLock();

        return proxy;
    }

    /**
     * @see #getOrMakeProxy(Object, ProxyRules)
     */
    @Nonnull
    public V8Object getOrMakeProxy(@Nullable final Object obj) {
        return getOrMakeProxy(obj, null);
    }

    /**
     * Make a {@link V8Array} from some raw {@link V8Value}.
     * <p>
     * This method is thread-safe, the lock will be acquired and then released.
     * Thus if you still need the lock, you need to reacquire it.
     *
     * @param v8Values - Some raw values.
     * @return A new {@link V8Array} containing the given values.
     */
    public V8Array makeArray(final V8Value... v8Values) {
        acquireLock();

        final V8Array array = new V8Array(runtime);

        for (V8Value value : v8Values) {
            array.push(value);
        }

        releaseLock();
        return array;
    }

    /**
     * Instantiate an object using the given statement as a reference to a constructor method.
     * The caller of this method is responsible for releasing the {@link V8Object} instance returned.
     * The caller of this method is also responsible for releasing the parameter array if provided.
     * <p>
     * This method is NOT thread-safe ! You must acquire the lock before using it.
     *
     * @param constructor  - A JS object representing an ES6 class.
     * @param constrParams - The array of parameters to pass to the constructor (optional).
     * @return A {@link V8Object} of the newly instantiated object. The caller must release it himself.
     */
    @Nonnull
    public V8Object createNewInstance(@Nonnull final V8Object constructor, @Nullable final V8Array constrParams) {
        // First argument is the class object, the second is a possibly-empty array of parameters
        try (final V8Array params = new V8Array(runtime)) {
            params.push(constructor);

            if (constrParams != null)
                params.push(constrParams);

            return (V8Object) newInstanceFn.call(null, params);
        }
    }

    /**
     * Overload of {@link #createNewInstance(V8Object, V8Array)}.
     *
     * @see #createNewInstance(V8Object, V8Array)
     */
    @Nonnull
    public V8Object createNewInstance(@Nonnull final V8Object constructor) {
        return createNewInstance(constructor, null);
    }
}
