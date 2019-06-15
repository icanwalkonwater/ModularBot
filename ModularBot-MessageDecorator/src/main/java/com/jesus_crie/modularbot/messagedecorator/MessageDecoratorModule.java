package com.jesus_crie.modularbot.messagedecorator;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.jesus_crie.modularbot.core.ModularBot;
import com.jesus_crie.modularbot.core.ModularBotBuildInfo;
import com.jesus_crie.modularbot.core.ModularBotBuilder;
import com.jesus_crie.modularbot.core.dependencyinjection.DefaultInjectionParameters;
import com.jesus_crie.modularbot.core.dependencyinjection.InjectorTarget;
import com.jesus_crie.modularbot.core.module.ModuleSettingsProvider;
import com.jesus_crie.modularbot.messagedecorator.decorator.MessageDecorator;
import com.jesus_crie.modularbot.core.module.Module;
import com.jesus_crie.modularbot.core.module.ModuleManager;
import com.jesus_crie.modularbot.nightconfig.NightConfigWrapperModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MessageDecoratorModule extends Module {

    private static final Logger LOG = LoggerFactory.getLogger("MessageDecorator");

    private static final ModuleInfo INFO = new ModuleInfo("Message Decorator",
            ModularBotBuildInfo.AUTHOR, ModularBotBuildInfo.GITHUB_URL,
            ModularBotBuildInfo.VERSION_NAME, ModularBotBuildInfo.BUILD_NUMBER());

    private static final int CLEANUP_PERIOD_SECOND = 1800; // 30min
    private static final String CONFIG_DECORATOR_CACHE = "_modularDecoratorCache";
    private static final String CACHE_MAIN_PATH = "decorators";

    private Map<Long, MessageDecorator<?>> decorators = Collections.emptyMap();
    private final FileConfig cache;

    @DefaultInjectionParameters
    private static final ModuleSettingsProvider DEFAULT_SETTINGS = new ModuleSettingsProvider("./decorator_cache.json");

    @InjectorTarget
    public MessageDecoratorModule(@Nonnull final NightConfigWrapperModule config, @Nonnull final String cachePath) {
        this(config, new File(cachePath));
    }

    public MessageDecoratorModule(@Nonnull final NightConfigWrapperModule config, @Nonnull final File cachePath) {
        super(INFO);
        LOG.info("Requested");

        cache = config.registerSingletonSecondaryConfig(CONFIG_DECORATOR_CACHE, cachePath);
    }

    @Override
    public void onShardsReady(@Nonnull final ModularBot bot) {
        super.onShardsReady(bot);

        loadCachedDecorators();

        bot.getMainPool().scheduleAtFixedRate(() -> LOG.info("Cleaned up " + cleanup() + " decorators"),
                CLEANUP_PERIOD_SECOND, CLEANUP_PERIOD_SECOND, TimeUnit.SECONDS);
    }

    @Override
    public void onShutdownShards() {
        cleanup();

        final List<Config> serializedDecorators = decorators.values().stream()
                .filter(decorator -> decorator instanceof Cacheable && decorator.isAlive())
                .map(decorator -> ((Cacheable) decorator).serialize())
                .collect(Collectors.toList());
        decorators.values().forEach(MessageDecorator::destroy);

        cache.set(CACHE_MAIN_PATH, serializedDecorators);

        LOG.info("Successfully serialized " + serializedDecorators.size() + " of " + decorators.size() + " registered decorators.");
    }

    /**
     * Load the cached decorators from the config.
     */
    @SuppressWarnings("unchecked")
    private void loadCachedDecorators() {
        final List<Config> data = cache.get(CACHE_MAIN_PATH);
        if (data == null)
            return;

        LOG.info("De-serializing " + data.size() + " decorators...");

        for (final Config serialized : data) {
            final String clazzS = serialized.get(Cacheable.KEY_CLASS);
            if (clazzS == null) {
                LOG.warn("Found cached decorator without class, skipping.");
                continue;
            }

            try {
                final Class<MessageDecorator<?>> clazz = (Class<MessageDecorator<?>>) Class.forName(clazzS);

                final Method tryDeserialize = clazz.getMethod("tryDeserialize", Config.class, ModularBot.class);
                final MessageDecorator<?> decorator = (MessageDecorator<?>) tryDeserialize.invoke(null, serialized, bot);

                if (decorator == null) {
                    LOG.debug("Deserialized decorator is null, assuming timeout was reached, ignoring silently.");
                    continue;
                }

                // Setup decorator and register.
                decorator.setup();
                registerDecorator(decorator);

            } catch (ClassNotFoundException e) {
                LOG.warn("Found cached decorator with unknown class, skipping. [" + clazzS + "]");
            } catch (NoSuchMethodException e) {
                LOG.warn("Cached decorator don't have the correct method signature to be deserialized, skipping. [" + clazzS + "]");
            } catch (IllegalAccessException e) {
                LOG.warn("Cached decorator #tryDeserialize method can't be accessed, skipping. [" + clazzS + "]");
            } catch (InvocationTargetException e) {
                LOG.warn("An exception was thrown while trying to deserialize a cached decorator, skipping. Reason: " + e.getTargetException());
            }
        }

        LOG.info("Successfully deserialized " + decorators.size() + " valid decorators.");
    }

    /**
     * Register a decorator in this module.
     * If you want your decorator to be serialized and restored after a shutdown, you need to register it.
     * <p>
     * Can also be performed by {@link MessageDecorator#register(MessageDecoratorModule)}.
     *
     * @param decorator The decorator to register.
     * @see MessageDecorator#register(MessageDecoratorModule)
     * @see MessageDecorator#register(ModularBot)
     */
    public void registerDecorator(@Nonnull final MessageDecorator<?> decorator) {
        if (decorators.size() == 0)
            decorators = new ConcurrentHashMap<>();

        decorators.compute(decorator.getBinding().getIdLong(), (key, old) -> {
            if (old != null) old.destroy();
            return decorator;
        });
    }

    /**
     * Destroy and unregister a bound decorator by its binding's id.
     * If the binding has not registered decorator attached, it will be ignored.
     *
     * @param bindingId The id of the decorator's binding.
     */
    public void unregisterBoundDecorator(final long bindingId) {
        decorators.computeIfPresent(bindingId, (key, val) -> {
            if (val.isAlive()) val.destroy();
            return null;
        });
    }

    public Collection<MessageDecorator<?>> getDecorators() {
        return Collections.unmodifiableCollection(decorators.values());
    }

    public int cleanup() {
        final int prev = decorators.size();
        decorators.entrySet().stream()
                .filter(e -> !e.getValue().isAlive())
                .forEach(e -> unregisterBoundDecorator(e.getKey()));
        return prev - decorators.size();
    }
}
