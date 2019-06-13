package com.jesus_crie.modularbot.v8support;

import com.eclipsesource.v8.NodeJS;
import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
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

    private static final String V8_NEW_PROP_NAME_PREFIX = "__j2v8__parameters_";

    private final NodeJS node;
    private final V8 runtime;
    private final List<V8ModuleWrapper> modules = new ArrayList<>();

    @InjectorTarget
    public V8SupportModule() {
        super(INFO);
        node = NodeJS.createNodeJS();
        runtime = node.getRuntime();
        LOG.info("Requested");
        LOG.info("Node.JS runtime created");
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
     * @param jsConstructorExpression - A JS expression that evaluates to a constructor.
     * @param params                  - The array of parameters to pass (optional).
     * @return A {@link V8Object} of the newly instantiated object. The caller must release it himself.
     */
    @Nonnull
    public V8Object createNewInstance(@Nonnull final String jsConstructorExpression, @Nullable final V8Array params) {
        // If no params
        if (params == null || params.isUndefined() || params.length() == 0)
            return createNewInstance(jsConstructorExpression);

        // Build a seemingly unique identifier
        final String paramsPropName = V8_NEW_PROP_NAME_PREFIX + params.hashCode();
        // Affect it to the global object
        final V8Object global = runtime.getObject("global");
        global.add(paramsPropName, params);

        // Instantiate object
        // This method isn't responsible for releasing it
        final V8Object obj = runtime.executeObjectScript(String.format("new %s(global.%s);", jsConstructorExpression, paramsPropName));

        // Unset the prop
        global.addUndefined(paramsPropName);

        global.release();
        return obj;
    }

    /**
     * Overload of {@link #createNewInstance(String, V8Array)}.
     *
     * @see #createNewInstance(String, V8Array)
     */
    @Nonnull
    public V8Object createNewInstance(@Nonnull final String jsConstructorExpression) {
        // Instantiate the object
        // This method isn't responsible for releasing it
        return runtime.executeObjectScript(String.format("new %s();", jsConstructorExpression));
    }

    @Override
    public void onUnload() {
        LOG.info("Releasing " + modules.size() + " modules...");
        modules.forEach(V8ModuleWrapper::release);
        node.release();
    }
}
