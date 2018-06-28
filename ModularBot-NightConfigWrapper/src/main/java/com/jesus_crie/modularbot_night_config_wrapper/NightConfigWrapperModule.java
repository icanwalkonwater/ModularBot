package com.jesus_crie.modularbot_night_config_wrapper;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.core.file.FileConfigBuilder;
import com.jesus_crie.modularbot.ModularBotBuildInfo;
import com.jesus_crie.modularbot.ModularBotBuilder;
import com.jesus_crie.modularbot.module.BaseModule;
import com.jesus_crie.modularbot.module.ModuleManager;
import com.jesus_crie.modularbot_night_config_wrapper.exception.DirectoryAccessDeniedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.RegEx;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class NightConfigWrapperModule extends BaseModule {

    private static final Logger LOG = LoggerFactory.getLogger("NightConfigWrapper");

    private static final ModuleInfo INFO = new ModuleInfo("Message Decorator", ModularBotBuildInfo.AUTHOR + ", TheElectronWill",
            ModularBotBuildInfo.GITHUB_URL, ModularBotBuildInfo.VERSION_NAME, ModularBotBuildInfo.BUILD_NUMBER());

    private ModuleManager moduleManager;

    private final FileConfigBuilder primaryConfigBuilder;
    private FileConfig primaryConfig;
    private Map<String, FileConfig> secondaryConfigs = Collections.emptyMap();

    public NightConfigWrapperModule() {
        super(INFO);
        primaryConfigBuilder = FileConfig.builder("./config.json")
                .defaultResource("/default_config.json")
                .autoreload()
                .concurrent();
    }

    public NightConfigWrapperModule(@Nonnull final String primaryConfigPath) {
        super(INFO);
        primaryConfigBuilder = FileConfig.builder(primaryConfigPath);
    }

    @Override
    public void onLoad(@Nonnull final ModuleManager moduleManager, @Nonnull ModularBotBuilder builder) {
        this.moduleManager = moduleManager;
    }

    @SuppressWarnings({"unchecked", "JavaReflectionInvocation"})
    @Override
    public void onInitialization() {
        primaryConfig = primaryConfigBuilder.build();
        primaryConfig.load();
        secondaryConfigs.values().forEach(FileConfig::load);

        final BaseModule commandModule = moduleManager.getModuleByClassName("com.jesus_crie.modularbot_command.CommandModule");
        if (commandModule == null)
            return;

        // Command module is present, use the information of the primary config for it.

        try {
            final Class<? extends BaseModule> commandModuleClass =
                    (Class<? extends BaseModule>) Class.forName("com.jesus_crie.modularbot_command.CommandModule");

            // Set the creator id from the config.
            Optional<Long> creator = primaryConfig.getOptional("creator_id");
            if (creator.isPresent()) {
                final Method setCreatorId = commandModuleClass.getDeclaredMethod("setCreatorId", long.class);

                LOG.debug("Setting creator id: " + creator.get());
                setCreatorId.invoke(commandModule, creator.get());
            }

            // Set the default prefix
            Optional<String> prefix = primaryConfig.getOptional("prefix");
            if (prefix.isPresent()) {
                final Field prefixField = commandModuleClass.getDeclaredField("defaultPrefix");
                prefixField.setAccessible(true);

                LOG.debug("Setting default prefix: " + prefix.get());
                prefixField.set(commandModule, prefix.get());
            }

            // Set the custom prefixes from the config.

            //Optional<List<Config>> customPrefix = primaryConfig.getOptional("guild_prefix");
            Optional<List<Object>> customPrefix = primaryConfig.getOptional("guild_prefix"); // TODO 27/06/2018 temporary fix
            if (customPrefix.isPresent()) {
                final Method addCustomPrefix = commandModuleClass.getMethod("addCustomPrefixForGuild", long.class, String.class);
                //for (Config config : customPrefix.get()) {
                for (Object v : customPrefix.get()) { // TODO 27/06/2018 temporary fix
                    Config config;
                    if (v instanceof Config)
                        config = (Config) v;
                    else continue;

                    LOG.debug("Adding prefix: " + config.get("prefix") + " for guild " + config.get("guild_id"));
                    addCustomPrefix.invoke(commandModule, config.get("guild_id"), config.get("prefix"));
                }
            }

        } catch (ReflectiveOperationException ignore) {
            ignore.printStackTrace();
        } // Should not occur.
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onUnload() {
        if (primaryConfig.getOptional("guild_prefix").isPresent()) {
            final BaseModule commandModule = moduleManager.getModuleByClassName("com.jesus_crie.modularbot_command.CommandModule");
            if (commandModule == null)
                return;

            try {
                final Class<? extends BaseModule> commandModuleClass =
                        (Class<? extends BaseModule>) Class.forName("com.jesus_crie.modularbot_command.CommandModule");
                final Method getCustomPrefixes = commandModuleClass.getMethod("getCustomPrefixes");

                final Map<Long, String> prefixes = (Map<Long, String>) getCustomPrefixes.invoke(commandModule);
                final List<Config> configs = prefixes.entrySet().stream()
                        .map(entry -> {
                            Config pair = Config.inMemory();
                            pair.set("guild_id", entry.getKey());
                            pair.set("prefix", entry.getValue());

                            return pair;
                        }).collect(Collectors.toList());

                primaryConfig.set("guild_prefix", configs);

            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ignore) {
            } // Will not occur because i'm too good for that =)
        }

        primaryConfig.save();
        primaryConfig.close();
        secondaryConfigs.values().forEach(config -> {
            config.save();
            config.close();
        });
    }

    /**
     * Return the {@link FileConfigBuilder FileConfigBuilder} that will be used to create the config file during
     * the initialization.
     *
     * @return The {@link FileConfigBuilder FileConfigBuilder}.
     */
    @Nonnull
    public FileConfigBuilder customizePrimaryConfig() {
        return primaryConfigBuilder;
    }

    /**
     * Register a new secondary config located at the given path.
     * <p>
     * Overload of {@link #useSecondaryConfig(String, File)}.
     *
     * @param path The path of the file, existing ot not.
     * @throws UnsupportedOperationException Thrown if the config has already been initialized.
     * @see #useSecondaryConfig(String, File)
     * @see #useSecondaryConfig(String, FileConfig)
     */
    public void useSecondaryConfig(@Nonnull final String path) {
        useSecondaryConfig(null, path);
    }

    /**
     * Register a new secondary config located at the given path.
     * <p>
     * Overload of {@link #useSecondaryConfig(String, File)}.
     *
     * @param configName The name of the config that will be used to query it.
     * @param path       The path of the file, existing ot not.
     * @throws UnsupportedOperationException Thrown if the config has already been initialized.
     * @see #useSecondaryConfig(String, File)
     * @see #useSecondaryConfig(String, FileConfig)
     */
    public void useSecondaryConfig(@Nullable final String configName, @Nonnull final String path) {
        useSecondaryConfig(configName, new File(path));
    }

    /**
     * Register a secondary config located at the given path.
     * You can override this if you want to change the default {@link FileConfigBuilder FileConfigBuilder} used for the
     * secondary configs.
     * <p>
     * Overload of {@link #useSecondaryConfig(String, FileConfig)} with a default builder.
     *
     * @param configName The name of the config that will be used to query it back.
     * @param path       The path of the file, existing or not.
     * @throws UnsupportedOperationException Thrown if the config has already been initialized.
     * @see #useSecondaryConfig(String, FileConfig)
     * @see #useSecondaryConfig(String, File)
     */
    public void useSecondaryConfig(@Nullable final String configName, @Nonnull final File path) {
        useSecondaryConfig(configName == null ? path.getName() : configName,
                FileConfig.builder(path)
                        .autoreload()
                        .concurrent()
                        .build()
        );
    }

    /**
     * Directly register the given config with the given name.
     * It will override other config files with this name after closing them.
     *
     * @param configName The name of the config that will be registered.
     * @param config     The config file to register.
     * @throws UnsupportedOperationException Thrown if the config has already been initialized.
     * @see #getSecondaryConfig(String)
     */
    public void useSecondaryConfig(@Nonnull final String configName, @Nonnull final FileConfig config) {
        // Replace the default empty map
        // This is like this to avoid a useless ConcurrentMap
        if (secondaryConfigs.size() == 0)
            secondaryConfigs = new ConcurrentHashMap<>();

        secondaryConfigs.compute(configName, (key, old) -> {
            if (old != null) old.close();
            return config;
        });
    }

    /**
     * Query an already registered secondary config file.
     *
     * @param configName The name of the config when it was registered.
     * @return The queried config file or {@code null}.
     * @see #useSecondaryConfig(String, FileConfig)
     */
    @Nullable
    public FileConfig getSecondaryConfig(@Nonnull final String configName) {
        return secondaryConfigs.get(configName);
    }

    /**
     * @see #loadConfigGroup(String, File, boolean, String)
     * @see #loadConfigGroup(String, String, boolean)
     */
    public void loadConfigGroup(@Nullable final String groupName, @Nonnull final String directory) {
        loadConfigGroup(groupName, directory, false);
    }

    /**
     * @see #loadConfigGroup(String, File, boolean, String)
     * @see #loadConfigGroup(String, String)
     */
    public void loadConfigGroup(@Nullable final String groupName, @Nonnull final String directory, boolean recursive) {
        loadConfigGroup(groupName, new File(directory), recursive, "");
    }

    /**
     * Load a group of config files from a directory and eventually subdirectories.
     * If {@code recursive} is {@code true}, the method will be recursively applied to all subdirectories.
     * You can also provide a pattern that the <b>file names</b> need to satisfy (and not the directories) to be
     * registered.
     * <p>
     * The method {@link #useSecondaryConfig(String, File)} is used to register the sub-configs and their name will be
     * the group name plus the name of the subdirectory plus the name of the file with it's extension.
     * <p>
     * The method {@link #getConfigGroup(String)} can be used to retrieve them.
     *
     * @param groupName      The name of the group. If null, will be the name of the directory.
     * @param directory      The directory.
     * @param recursive      If the subdirectories need to be loaded recursively.
     * @param includePattern A pattern to specify files to include.
     * @throws IllegalArgumentException       If the provided directory isn't a folder.
     * @throws DirectoryAccessDeniedException If we can't create the directory.
     * @see #loadConfigGroup(String, String, boolean)
     * @see #addSecondaryConfigToGroup(String, FileConfig)
     * @see #getConfigGroup(String)
     */
    public void loadConfigGroup(@Nullable String groupName, @Nonnull final File directory, boolean recursive,
                                @Nonnull @RegEx final String includePattern) {
        if (!directory.isDirectory() && directory.exists())
            throw new IllegalArgumentException("The provided path isn't a directory !");

        if (!directory.exists() && !directory.mkdirs())
            throw new DirectoryAccessDeniedException("Can't create the given directory, maybe some permissions are missing.");

        if (groupName == null)
            groupName = directory.getName();

        final File[] content = directory.listFiles(file -> {
            // If is file and there is a pattern for files to exclude.
            if (file.isFile() && includePattern.length() != 0)
                return file.getName().matches(includePattern);

                // Or there is just a file.
            else if (file.isFile())
                return true;

                // Or its a directory.
            else return recursive;
        });

        // If the directory is empty.
        if (content == null)
            return;

        for (File file : content) {
            if (file.isFile())
                useSecondaryConfig(groupName + "." + file.getName(), file);
            else if (recursive)
                loadConfigGroup(groupName + "." + file.getName(), file, true, includePattern);
        }
    }

    /**
     * Get all of the {@link FileConfig FileConfig} that belongs to que given group, = their name begin with the
     * group name and is followed by a "." and the reste of the config name.
     *
     * @param groupName The name of the group to query.
     * @return A list containing all of the {@link FileConfig FileConfig} of the group.
     */
    public List<FileConfig> getConfigGroup(@Nonnull final String groupName) {
        return secondaryConfigs.keySet().stream()
                .filter(k -> k.startsWith(groupName + "."))
                .map(k -> secondaryConfigs.get(k))
                .collect(Collectors.toList());
    }

    /**
     * Overload of {@link #addSecondaryConfigToGroup(String, File)}.
     *
     * @param groupName The group that owns this config.
     * @param path      The path to the config file.
     * @see #addSecondaryConfigToGroup(String, File)
     * @see #addSecondaryConfigToGroup(String, FileConfig)
     */
    public void addSecondaryConfigToGroup(@Nonnull final String groupName, @Nonnull final String path) {
        addSecondaryConfigToGroup(groupName, new File(path));
    }

    /**
     * This method is similar to {@link #addSecondaryConfigToGroup(String, FileConfig)} and delegates to the method
     * {@link #useSecondaryConfig(String, File)} to save the file.
     *
     * @param groupName The name of the group that owns the config.
     * @param path      Represent the path of the config file.
     * @see #useSecondaryConfig(String, FileConfig)
     */
    public void addSecondaryConfigToGroup(@Nonnull final String groupName, @Nonnull final File path) {
        useSecondaryConfig(groupName + "." + path.getName(), path);
    }

    /**
     * Append a new config file to an existing or not, config group.
     * This is an alternative to {@link #loadConfigGroup(String, File, boolean, String)} when the config file isn't in
     * the same folder or if the config is created procedurally.
     * <p>
     * This method delegate to {@link #useSecondaryConfig(String, FileConfig)}.
     *
     * @param groupName The name of the group that owns this config.
     * @param config    The actual config.
     * @see #loadConfigGroup(String, File, boolean, String)
     */
    public void addSecondaryConfigToGroup(@Nonnull final String groupName, @Nonnull final FileConfig config) {
        useSecondaryConfig(groupName + "." + config.getFile().getName(), config);
    }

    /**
     * Get the main {@link FileConfig FileConfig}
     *
     * @return The main config file.
     * @throws IllegalStateException If the module hasn't been initialized yet.
     */
    @Nonnull
    public FileConfig getPrimaryConfig() {
        if (primaryConfig == null)
            throw new IllegalStateException("You can't query the config before the initialization !");
        return primaryConfig;
    }
}
