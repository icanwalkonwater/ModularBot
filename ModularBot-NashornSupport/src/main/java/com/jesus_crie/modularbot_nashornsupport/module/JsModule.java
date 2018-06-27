package com.jesus_crie.modularbot_nashornsupport.module;

import jdk.nashorn.api.scripting.NashornScriptEngine;

import javax.annotation.Nonnull;
import javax.script.ScriptException;
import java.io.File;

public class JsModule {

    private final NashornScriptEngine engine;

    private final File scriptLocation;
    private final BaseJsModule jsModule;

    public JsModule(@Nonnull final NashornScriptEngine engine, @Nonnull final File scriptLocation) throws ScriptException {
        this.engine = engine;
        this.scriptLocation = scriptLocation;

        try {
            jsModule = (BaseJsModule) engine.invokeFunction("getModule");
        } catch (NoSuchMethodException e) {
            throw new ScriptException(e);
        }
    }

    @Nonnull
    public NashornScriptEngine getEngine() {
        return engine;
    }

    @Nonnull
    public File getScriptLocation() {
        return scriptLocation;
    }

    @Nonnull
    public BaseJsModule getJsModule() {
        return jsModule;
    }
}
