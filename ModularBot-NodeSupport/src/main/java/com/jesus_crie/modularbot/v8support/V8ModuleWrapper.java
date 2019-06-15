package com.jesus_crie.modularbot.v8support;

import com.eclipsesource.v8.Releasable;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import com.jesus_crie.modularbot.core.module.Module;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;

public abstract class V8ModuleWrapper extends Module implements Releasable {

    private final File mainFile;
    private V8Object nodeModule;

    public V8ModuleWrapper(@Nonnull final V8SupportModule module, @Nonnull final File mainFile, @Nullable final V8Array parameters) {
        super();
        this.mainFile = mainFile;
        setup(module, parameters);
    }

    public V8ModuleWrapper(@Nonnull final ModuleInfo info, @Nonnull final V8SupportModule module,
                           @Nonnull final File mainFile, @Nullable final V8Array parameters) {
        super(info);
        this.mainFile = mainFile;
        setup(module, parameters);
    }

    private void setup(@Nonnull final V8SupportModule module, @Nullable final V8Array parameters) {
        // Gather the exported module constructor
        final V8Object moduleConstructor = module.getNode().require(mainFile);

        // Create the instance of the module with the exported constructor
        nodeModule = module.createNewInstance(moduleConstructor, parameters);
        moduleConstructor.release();

        module.registerModule(this);
    }

    @Nonnull
    public V8Object getModuleObject() {
        return nodeModule;
    }

    @Override
    public void release() {
        nodeModule.release();
    }
}
