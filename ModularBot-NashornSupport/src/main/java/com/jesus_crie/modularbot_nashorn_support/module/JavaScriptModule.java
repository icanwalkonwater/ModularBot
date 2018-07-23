package com.jesus_crie.modularbot_nashorn_support.module;

import jdk.nashorn.api.scripting.NashornScriptEngine;

import javax.annotation.Nonnull;
import javax.script.ScriptException;
import java.io.File;

/**
 * Holds the required background for a JS module to be executed.
 */
public class JavaScriptModule {

    private final NashornScriptEngine engine;

    private final File scriptLocation;
    private final BaseJavaScriptModule jsModule;

    /**
     * Query the JS module from the {@link NashornScriptEngine NashornScriptEngine} that contains its code.
     *
     * @param engine         The script engine that runs the code of the module.
     * @param scriptLocation The location of the entry point of the module.
     * @throws ScriptException If it fails to get the module from the script.
     */
    public JavaScriptModule(@Nonnull final NashornScriptEngine engine, @Nonnull final File scriptLocation) throws ScriptException {
        this.engine = engine;
        this.scriptLocation = scriptLocation;

        try {
            jsModule = (BaseJavaScriptModule) engine.invokeFunction("getModule");
        } catch (NoSuchMethodException e) {
            throw new ScriptException(e);
        }
    }

    /**
     * @return The {@link NashornScriptEngine NashornScriptEngine} that runs the module.
     */
    @Nonnull
    public NashornScriptEngine getEngine() {
        return engine;
    }

    /**
     * @return The location of the entry point of the module.
     */
    @Nonnull
    public File getScriptLocation() {
        return scriptLocation;
    }

    /**
     * @return Get the JS module provided by the script.
     */
    @Nonnull
    public BaseJavaScriptModule getJsModule() {
        return jsModule;
    }
}
