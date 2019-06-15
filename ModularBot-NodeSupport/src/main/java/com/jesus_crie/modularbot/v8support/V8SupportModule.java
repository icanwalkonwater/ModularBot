package com.jesus_crie.modularbot.v8support;

import com.eclipsesource.v8.*;
import com.jesus_crie.modularbot.core.ModularBotBuildInfo;
import com.jesus_crie.modularbot.core.dependencyinjection.InjectorTarget;
import com.jesus_crie.modularbot.core.module.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class V8SupportModule extends Module {

    private static final Logger LOG = LoggerFactory.getLogger("V8Support");

    private static final ModuleInfo INFO = new ModuleInfo("V8/Node.JS Support",
            ModularBotBuildInfo.AUTHOR, ModularBotBuildInfo.GITHUB_URL,
            ModularBotBuildInfo.VERSION_NAME, ModularBotBuildInfo.BUILD_NUMBER());

    private static final String V8_NEW_INSTANCE_HELPER_SCRIPT = "(() => function (constr, args) { return new constr(...args || []); })()";

    private final NodeJS node;
    private final V8 runtime;
    private final V8Function newInstanceFn;
    private final List<V8ModuleWrapper> modules = new ArrayList<>();

    @InjectorTarget
    public V8SupportModule() {
        super(INFO);
        LOG.info("Requested");

        node = NodeJS.createNodeJS();
        runtime = node.getRuntime();
        newInstanceFn = (V8Function) runtime.executeObjectScript(V8_NEW_INSTANCE_HELPER_SCRIPT);

        LOG.info("Node.JS runtime configured");
    }

    public NodeJS getNode() {
        return node;
    }

    public void registerModule(@Nonnull final V8ModuleWrapper wrapper) {
        modules.add(wrapper);
    }

    /**
     * Instantiate an object using the given statement as a reference to a constructor method.
     * The caller of this method is responsible for releasing the {@link V8Object} instance returned.
     * The caller of this method is also responsible for releasing the parameter array if provided.
     *
     * @param constructor  - A JS object representing an ES6 class.
     * @param constrParams - The array of parameters to pass to the constructor (optional).
     * @return A {@link V8Object} of the newly instantiated object. The caller must release it himself.
     */
    @Nonnull
    public V8Object createNewInstance(@Nonnull final V8Object constructor, @Nullable final V8Array constrParams) {
        // First argument is the class object, the second is a possibly-empty array of parameters
        final V8Array params = new V8Array(runtime);
        params.push(constructor);

        if (constrParams != null)
            params.push(constrParams);

        final V8Object obj = (V8Object) newInstanceFn.call(null, params);
        params.release();

        return obj;
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

    @Override
    public void onUnload() {
        LOG.info("Releasing " + modules.size() + " modules...");
        modules.forEach(V8ModuleWrapper::release);
        newInstanceFn.release();
        node.release();
    }
}
