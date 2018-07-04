package com.jesus_crie.modularbot_message_decorator;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.jesus_crie.modularbot.ModularBot;
import com.jesus_crie.modularbot.ModularBotBuildInfo;
import com.jesus_crie.modularbot.ModularBotBuilder;
import com.jesus_crie.modularbot.module.BaseModule;
import com.jesus_crie.modularbot.module.ModuleManager;
import com.jesus_crie.modularbot_message_decorator.decorator.MessageDecorator;
import com.jesus_crie.modularbot_night_config_wrapper.NightConfigWrapperModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MessageDecoratorModule extends BaseModule {

    private static final ModuleInfo INFO = new ModuleInfo("Message Decorator",
            ModularBotBuildInfo.AUTHOR, ModularBotBuildInfo.GITHUB_URL,
            ModularBotBuildInfo.VERSION_NAME, ModularBotBuildInfo.BUILD_NUMBER());

    private static final Logger LOG = LoggerFactory.getLogger("MessageDecoratorModule");

    private static final String CONFIG_DECORATOR_CACHE = "modularDecoratorCache";
    private static final String CACHE_MAIN_PATH = "decorators";

    private Map<Long, MessageDecorator<?>> decorators = Collections.emptyMap();
    private final File cacheFile;
    private FileConfig cache;

    public MessageDecoratorModule() {
        this("./decorator_cache.json");
    }

    public MessageDecoratorModule(@Nonnull String cachePath) {
        this(new File(cachePath));
    }

    public MessageDecoratorModule(@Nonnull File cachePath) {
        super(INFO);
        cacheFile = cachePath;
    }

    @Override
    public void onLoad(@Nonnull final ModuleManager moduleManager, @Nonnull final ModularBotBuilder builder) {
        NightConfigWrapperModule config = moduleManager.getModule(NightConfigWrapperModule.class);
        if (config == null) throw new IllegalStateException("You need to register the module NightConfigWrapperModule prior to this module !");

        config.useSecondaryConfig(CONFIG_DECORATOR_CACHE, cacheFile);
        cache = config.getSecondaryConfig(CONFIG_DECORATOR_CACHE);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onShardsReady(@Nonnull final ModularBot bot) {
        super.onShardsReady(bot);

        List<Config> data = cache.get(CACHE_MAIN_PATH);
        for (Config serialized : data) {
            String clazzS = serialized.get(Cacheable.KEY_CLASS);
            if (clazzS == null) {
                LOG.warn("Found cached decorator without class, skipping.");
                continue;
            }

            try {
                final Class<MessageDecorator<?>> clazz = (Class<MessageDecorator<?>>) Class.forName(clazzS);

                final Method tryDeserialize = clazz.getMethod("tryDeserialize", Config.class, ModularBot.class);
                final MessageDecorator<?> decorator = (MessageDecorator<?>) tryDeserialize.invoke(null, serialized, bot);

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
    }

    @Override
    public void onShutdownShards() {
        decorators.values().forEach(MessageDecorator::destroy);
        final List<Config> serializedDecorators = decorators.values().stream()
                .filter(decorator -> decorator instanceof Cacheable)
                .map(decorator -> ((Cacheable) decorator).serialize())
                .collect(Collectors.toList());

        cache.set(CACHE_MAIN_PATH, serializedDecorators);
    }

    public void registerDecorator(@Nonnull final MessageDecorator<?> decorator) {
        decorators.put(decorator.getBinding().getIdLong(), decorator);
    }
}
