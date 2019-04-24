package com.jesus_crie.modularbot.nightconfig;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.core.file.FileConfigBuilder;
import com.electronwill.nightconfig.core.io.ParsingMode;
import com.electronwill.nightconfig.json.JsonFormat;
import com.jesus_crie.modularbot.ModularBotBuildInfo;
import com.jesus_crie.modularbot.module.BaseModule;
import com.jesus_crie.modularbot.module.ModuleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NightConfigWrapperModule extends BaseModule {

    private static final Logger LOG = LoggerFactory.getLogger("NightConfigWrapper");

    private static final ModuleInfo INFO = new ModuleInfo("NightConfig Wrapper",
            ModularBotBuildInfo.AUTHOR + ", TheElectronWill",
            ModularBotBuildInfo.GITHUB_URL + ", https://github.com/TheElectronWill/Night-Config",
            ModularBotBuildInfo.VERSION_NAME, ModularBotBuildInfo.BUILD_NUMBER());

    private static final String DEFAULT_SECONDARY_GROUP = "__default";

    private FileConfig primaryConfig;
    private Map<String, Set<FileConfig>> configGroups = Collections.emptyMap();

    public NightConfigWrapperModule() {
        this("./config.json");
    }

    public NightConfigWrapperModule(@Nonnull final String path) {
        this((FileConfigBuilder) FileConfig.builder(path, JsonFormat.minimalInstance())
                .defaultResource("/default_config.json")
                .charset(Charset.forName("UTF-8"))
                .parsingMode(ParsingMode.REPLACE)
                .concurrent()
        );
    }

    public NightConfigWrapperModule(@Nonnull final FileConfigBuilder builder) {
        super(INFO);
        primaryConfig = builder.build();
    }

    @Override
    public void onInitialization(@Nonnull final ModuleManager moduleManager) {
        loadEveryConfigs();
        loadCommandModuleSettings(moduleManager);
    }

    @Override
    public void onShutdownShards() {
        saveEveryConfigs();
    }

    @Override
    public void onUnload() {
        closeEveryConfigs();
    }

    /**
     * Overload of {@link #registerSecondaryConfig(String, String)} with a default group name.
     *
     * @param path - The name of the config to register.
     */
    public void registerSecondaryConfig(@Nonnull final String path) {
        registerSecondaryConfig(DEFAULT_SECONDARY_GROUP, path);
    }

    /**
     * Overload of {@link #registerSecondaryConfig(String, File)} with a default group name.
     *
     * @param file - The config to register.
     */
    public void registerSecondaryConfig(@Nonnull final File file) {
        registerSecondaryConfig(DEFAULT_SECONDARY_GROUP, file);
    }

    /**
     * Overload of {@link #registerSecondaryConfig(String, FileConfig)} with a default group name.
     *
     * @param config - The config to register.
     */
    public void registerSecondaryConfig(@Nonnull final FileConfig config) {
        registerSecondaryConfig(DEFAULT_SECONDARY_GROUP, config);
    }

    /**
     * Overload of {@link #registerSecondaryConfig(String, File)} with a plain path.
     *
     * @param groupName - The name of the group.
     * @param path      - The path of the config to register.
     */
    public void registerSecondaryConfig(@Nonnull final String groupName, @Nonnull final String path) {
        registerSecondaryConfig(groupName, new File(path));
    }

    /**
     * Overload of {@link #registerSecondaryConfig(String, FileConfig)} with a default config builder.
     *
     * @param groupName - The name of the group.
     * @param file      - The config to register.
     */
    public void registerSecondaryConfig(@Nonnull final String groupName, @Nonnull final File file) {
        registerSecondaryConfig(groupName, FileConfig.builder(file, JsonFormat.minimalInstance())
                .charset(Charset.forName("UTF-8"))
                .parsingMode(ParsingMode.REPLACE)
                .build());
    }

    /**
     * Register a config in the given group config.
     * A given config can only appear once in each group.
     * If the group doesn't exist, it will be created.
     *
     * @param groupName - The name of the group, case-sensitive.
     * @param config    - The config to register.
     * @throws UnsupportedOperationException If the group is already a singleton.
     */
    public void registerSecondaryConfig(@Nonnull final String groupName, @Nonnull final FileConfig config) {
        // Ensure secondary config map is a real map.
        ensureRealSecondaryConfigMap();

        final Set<FileConfig> group;
        if (!configGroups.containsKey(groupName))
            group = configGroups.put(groupName, new HashSet<>());
        else group = configGroups.get(groupName);

        group.add(config);
    }

    /**
     * Overload of {@link #registerSingletonSecondaryConfig(String, File)} with a plain path.
     *
     * @param name - The name of the config.
     * @param path - The path of the config to register.
     */
    public void registerSingletonSecondaryConfig(@Nonnull final String name, @Nonnull final String path) {
        registerSingletonSecondaryConfig(name, new File(path));
    }

    /**
     * Overload of {@link #registerSingletonSecondaryConfig(String, FileConfig)} with a default config builder.
     *
     * @param name - The name of the config.
     * @param file - The config to register.
     */
    public void registerSingletonSecondaryConfig(@Nonnull final String name, @Nonnull final File file) {
        registerSingletonSecondaryConfig(name, FileConfig.builder(file, JsonFormat.minimalInstance())
                .charset(Charset.forName("UTF-8"))
                .parsingMode(ParsingMode.REPLACE)
                .build());
    }

    /**
     * Register a config group with only the given config.
     * Throw an exception if it already exists or if you try to add another value.
     * Useful to make named configs.
     *
     * @param name   - The name of the config.
     * @param config - The config to register.
     * @throws IllegalStateException If the group already exists.
     */
    public void registerSingletonSecondaryConfig(@Nonnull final String name, @Nonnull final FileConfig config) {
        // Ensure secondary config map is a real map
        ensureRealSecondaryConfigMap();

        if (configGroups.containsKey(name))
            throw new IllegalStateException("Another config group has that name !");

        configGroups.put(name, Collections.singleton(config));
    }

    /**
     * Overload of {@link #getSecondaryConfigGroup(String)} for the default group.
     *
     * @return The config files from the default group or an empty set.
     */
    @Nonnull
    public Set<FileConfig> getSecondaryConfigGroup() {
        return getSecondaryConfigGroup(DEFAULT_SECONDARY_GROUP);
    }

    /**
     * Query a whole group of config files by its name.
     * Return an empty set if the config group doesn't exists or is empty.
     *
     * @param groupName - The name of the group to query.
     * @return A set of the config files that belongs to the given group or an empty set.
     */
    @Nonnull
    public Set<FileConfig> getSecondaryConfigGroup(@Nonnull final String groupName) {
        return configGroups.getOrDefault(groupName, Collections.emptySet());
    }

    /**
     * Overload of {@link #getSecondaryConfigFile(String)} for the default group.
     *
     * @return A config file that belongs to the default group.
     * @throws IllegalArgumentException If there is nothing in the default group or if it doesn't exist.
     */
    @Nonnull
    public FileConfig getSecondaryConfigFile() {
        return getSecondaryConfigFile(DEFAULT_SECONDARY_GROUP);
    }

    /**
     * Query a config file from a group, there are no guaranty that it will be the
     * same every time.
     * Useful when you use config groups to store named configs.
     *
     * @param groupName - The name of the group to query.
     * @return A config file that belongs to the given group.
     * @throws IllegalArgumentException If the given group is empty or doesn't exist.
     */
    @Nonnull
    public FileConfig getSecondaryConfigFile(@Nonnull final String groupName) {
        final Set<FileConfig> group = configGroups.get(groupName);

        if (group != null && !group.isEmpty())
            return group.iterator().next();

        throw new IllegalArgumentException(String.format("The config group '%s' doesn't exist !", groupName));
    }

    /**
     * Trigger the loading of every config file.
     */
    private void loadEveryConfigs() {
        primaryConfig.load();
        configGroups.values().forEach(set -> set.forEach(FileConfig::load));
    }

    /**
     * Trigger the save of every config file.
     */
    private void saveEveryConfigs() {
        primaryConfig.save();
        configGroups.values().forEach(set -> set.forEach(FileConfig::close));
    }

    /**
     * Close every config file.
     */
    private void closeEveryConfigs() {
        primaryConfig.close();
        configGroups.values().forEach(set -> set.forEach(FileConfig::close));
    }

    /**
     * Load some default settings from the primary config into the command module.
     *
     * @param moduleManager - The module manager instance.
     */
    @SuppressWarnings("unchecked")
    private void loadCommandModuleSettings(@Nonnull final ModuleManager moduleManager) {
        final BaseModule module = moduleManager.getModuleByClassName("com.jesus_crie.modularbot.command.CommandModule");

        LOG.info("Configuring command module...");
        try {
            final Class<? extends BaseModule> commandModuleClass =
                    (Class<? extends BaseModule>) Class.forName("com.jesus_crie.modularbot.command.CommandModule");

            // Set creator ID
            primaryConfig.getOptionalLong("creator_id").ifPresent(creator -> {
                LOG.info("[Command Module] Setting creator id: " + creator);
                try {
                    commandModuleClass.getMethod("setCreatorId", long.class).invoke(module, creator);
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    LOG.info("[Command Module] Failed to set creator id, ignoring.");
                }
            });

            // Set default prefix
            primaryConfig.<String>getOptional("prefix").ifPresent(prefix -> {
                LOG.info("[Command Module] Setting default command prefix: " + prefix);
                try {
                    final Field field = commandModuleClass.getField("defaultPrefix");
                    field.setAccessible(true);
                    field.set(module, prefix);
                } catch (IllegalAccessException | NoSuchFieldException e) {
                    LOG.info("[Command Module] Failed to set default prefix, ignoring.");
                }
            });

            // Add custom prefixes
            primaryConfig.<List<Config>>getOptional("guild_prefix").ifPresent(prefixes -> {
                try {
                    final Method method = commandModuleClass.getMethod("addCustomPrefixForGuild", long.class, String.class);

                    for (Config prefixConfig : prefixes) {
                        final long guildId = prefixConfig.getLong("guild_id");
                        final String prefix = prefixConfig.get("prefix");
                        LOG.info("[Command Module] Setting custom prefix %s for guild %s", prefix, guildId);
                        method.invoke(module, guildId, prefix);
                    }
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    LOG.info("[Command Module] Failed to add custom prefixes, ignoring.");
                }
            });

        } catch (ClassNotFoundException e) {
            LOG.info("Command module not found, ignoring.");
        }
    }

    private void ensureRealSecondaryConfigMap() {
        if (configGroups == null || configGroups.isEmpty())
            configGroups = new ConcurrentHashMap<>();
    }
}
