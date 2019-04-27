package com.jesus_crie.modularbot.graalvm;

import com.jesus_crie.modularbot.core.ModularBot;
import com.jesus_crie.modularbot.core.ModularBotBuilder;
import com.jesus_crie.modularbot.core.module.Module;
import com.jesus_crie.modularbot.core.module.ModuleManager;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Language;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.function.Supplier;

/**
 * Wrapper around a module written in any language supported by GraalVM.
 * This wrapper exists to allow the usage of the dependency injection within a guest language
 * and to provide an kind of middleware to define conversions to and from a module.
 */
public abstract class GraalModuleWrapper extends Module {

    private Language language;
    private Context context;
    private Source source;
    private Value clazz;
    private Value instance;

    protected GraalModuleWrapper(@Nonnull final ModuleInfo info, @Nonnull final File mainFile, @Nonnull final Object... args) {
        super(info);
        init(mainFile, args);
    }

    protected GraalModuleWrapper(@Nonnull final File mainFile, @Nonnull final Object... args) {
        init(mainFile, args);
    }

    private void init(@Nonnull final File mainFile, @Nonnull final Object... args) {
        try {
            // Find the language of the module
            final String strLang = Source.findLanguage(mainFile);

            // Create a source based on that
            source = Source.newBuilder(strLang, mainFile)
                    .encoding(Charset.forName("UTF-8"))
                    .cached(false) // Evaluated one time
                    .internal(true)
                    .name(getClass().getSimpleName()) // Same name as this class
                    .build();

            // Build the context with full permissions
            context = Context.newBuilder(strLang)
                    .allowAllAccess(true)
                    .build();

            // Query the language object for fun
            language = context.getEngine().getLanguages().get(strLang);
            // Evaluate the file (no side effects should be triggered, only declarations)
            context.eval(source);

            // Get the exported class of the module and instantiate it
            clazz = context.getPolyglotBindings().getMember("class");
            instance = clazz.newInstance(args);

            // Set some key methods on the guest
            instance.putMember("getBot", (Supplier<ModularBot>) this::getBot);
            instance.putMember("getInfo", (Supplier<ModuleInfo>) this::getInfo);
            // Convert the state to a number for convenience
            instance.putMember("getState", (Supplier<Integer>) () -> getState().ordinal());

        } catch (IOException e) {
            throw new IllegalStateException("Failed to create source from file " + mainFile);
        }
    }

    // *** Lifecycle methods ***

    @Override
    public final void onLoad(@Nonnull final ModuleManager moduleManager, @Nonnull final ModularBotBuilder builder) {
        safeInvoke("onLoad", moduleManager, builder);
    }

    @Override
    public final void onInitialization(@Nonnull final ModuleManager moduleManager) {
        safeInvoke("onInitialization", moduleManager);
    }

    @Override
    public final void onPostInitialization() {
        safeInvoke("onPostInitialization");
    }

    @Override
    public final void onPrepareShards() {
        safeInvoke("onPrepareShards");
    }

    @Override
    public final void onShardsCreated() {
        safeInvoke("onShardsCreated");
    }

    @Override
    public final void onShardsReady(@Nonnull ModularBot bot) {
        super.onShardsReady(bot);
        safeInvoke("onShardsReady", bot);
    }

    @Override
    public final void onShutdownShards() {
        safeInvoke("onShutdownShards");
    }

    @Override
    public final void onUnload() {
        safeInvoke("onUnload");
    }

    // *** General use methods ***

    /**
     * Safely invoke the member function.
     * Will not trigger any error if the target object doesn't have the invokable member.
     *
     * @param identifier - The identifier of the member to invoke.
     * @param args       - The arguments to provide to the function.
     * @return The value returned by the member, or null if the member can't be invoked.
     */
    @Nonnull
    protected Value safeInvoke(@Nonnull final String identifier, @Nonnull final Object... args) {
        if (!instance.canInvokeMember(identifier))
            return context.asValue(null);

        return instance.invokeMember(identifier, args);
    }

    /**
     * @return The language in which this module is written.
     */
    @Nonnull
    public Language getLanguage() {
        return language;
    }

    /**
     * @return The Graal context of execution specific to this module.
     */
    @Nonnull
    protected Context getContext() {
        return context;
    }

    /**
     * @return The source of this module.
     */
    @Nonnull
    protected Source getSource() {
        return source;
    }

    /**
     * @return Get the class definition of the module in the guest language.
     */
    @Nonnull
    protected Value getClazz() {
        return clazz;
    }

    /**
     * @return Get the instance of the module in the guest language.
     */
    @Nonnull
    public Value getInstance() {
        return instance;
    }
}
