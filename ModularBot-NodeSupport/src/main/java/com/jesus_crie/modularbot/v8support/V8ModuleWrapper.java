package com.jesus_crie.modularbot.v8support;

import com.eclipsesource.v8.Releasable;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import com.eclipsesource.v8.V8Value;
import com.jesus_crie.modularbot.core.ModularBot;
import com.jesus_crie.modularbot.core.ModularBotBuilder;
import com.jesus_crie.modularbot.core.module.Module;
import com.jesus_crie.modularbot.core.module.ModuleManager;
import com.jesus_crie.modularbot.v8support.proxying.ProxyRules;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;

public abstract class V8ModuleWrapper extends Module implements Releasable {

    protected final V8SupportModule module;

    protected final File mainFile;
    protected V8Object nodeModule;

    public V8ModuleWrapper(@Nonnull final V8SupportModule module, @Nonnull final File mainFile, @Nullable final V8Array parameters) {
        super();
        this.module = module;
        this.mainFile = mainFile;
        setup(module, parameters);
    }

    public V8ModuleWrapper(@Nonnull final ModuleInfo info, @Nonnull final V8SupportModule module,
                           @Nonnull final File mainFile, @Nullable final V8Array parameters) {
        super(info);
        this.module = module;
        this.mainFile = mainFile;
        setup(module, parameters);
    }

    private void setup(@Nonnull final V8SupportModule module, @Nullable final V8Array parameters) {
        module.acquireLock();

        // Gather the exported module constructor
        try (final V8Object moduleConstructor = module.getNode().require(mainFile)) {

            // Check that we can indeed invoke the constructor
            if (moduleConstructor.getV8Type() != V8Value.V8_FUNCTION) {
                throw new IllegalArgumentException("Exported module isn't a function ! " +
                        "Found type: " + V8Value.getStringRepresentation(moduleConstructor.getV8Type()));
            }

            // Create the instance of the module with the exported constructor
            nodeModule = module.createNewInstance(moduleConstructor, parameters);
        }

        if (parameters != null)
            parameters.close();

        module.registerModule(this);
        module.releaseLock();
    }

    @Nonnull
    public File getMainFile() {
        return mainFile;
    }

    @Nonnull
    public V8Object getModuleObject() {
        return nodeModule;
    }

    protected Object safeInvoke(@Nonnull final String functionName, @Nonnull final Object... parameters) {
        module.acquireLock();
        try {

            if (nodeModule.getType(functionName) == V8Value.V8_FUNCTION) {
                return nodeModule.executeJSFunction(functionName, parameters);
            } else {
                return null;
            }

        } finally {
            module.releaseLock();
        }
    }

    protected V8Value wrap(@Nonnull final Object object) {
        if (object instanceof V8Value)
            return (V8Value) object;

        return module.getOrMakeProxy(object);
    }

    protected V8Value wrap(@Nonnull final Object object, @Nullable final ProxyRules rules) {
        if (object instanceof V8Value)
            return (V8Value) object;

        return module.getOrMakeProxy(object, rules);
    }

    @Override
    public void close() {
        nodeModule.close();
    }

    @Deprecated
    @Override
    public void release() {
        close();
    }

    @Override
    public void onLoad(@Nonnull final ModuleManager moduleManager, @Nonnull final ModularBotBuilder builder) {
        safeInvoke("onLoad", wrap(moduleManager), wrap(builder));
    }

    @Override
    public void onInitialization(@Nonnull final ModuleManager moduleManager) {
        safeInvoke("onInitialization");
    }

    @Override
    public void onPostInitialization() {
        safeInvoke("onPostInitialization");
    }

    @Override
    public void onPrepareShards() {
        safeInvoke("onPrepareShards");
    }

    @Override
    public void onShardsCreated() {
        safeInvoke("onShardsCreated");
    }

    @Override
    public void onShardsReady(@Nonnull final ModularBot bot) {
        super.onShardsReady(bot);
        safeInvoke("onShardsReady", wrap(bot));
    }

    @Override
    public void onShutdownShards() {
        safeInvoke("onShutdownShards");
    }

    @Override
    public void onUnload() {
        safeInvoke("onUnload");
    }
}
