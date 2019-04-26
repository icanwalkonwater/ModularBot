package com.jesus_crie.modularbot.core.module;

import com.jesus_crie.modularbot.core.ModularBot;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class Module implements Lifecycle {

    /**
     * Package private field, describe the state of the module.
     */
    Lifecycle.State state = State.STOPPED;

    protected ModuleInfo info;

    /**
     * Reference to the instance of {@link ModularBot}.
     * Only available if state {@link Lifecycle.State#STARTED} has been reached and {@link #onShardsReady(ModularBot)}
     * has been called.
     */
    protected ModularBot bot;

    /**
     * Create a module instance without any infos (all field are set to 'Unknown').
     */
    protected Module() {
        this(ModuleInfo.EMPTY);
    }

    /**
     * Create a module instance with the given infos.
     *
     * @param info - The infos of the module.
     */
    protected Module(@Nonnull final ModuleInfo info) {
        this.info = info;
    }

    @Override
    public void onShardsReady(@Nonnull final ModularBot bot) {
        this.bot = bot;
    }

    @Nonnull
    public ModuleInfo getInfo() {
        return info;
    }

    @Nonnull
    public State getState() {
        return state;
    }

    public ModularBot getBot() {
        return bot;
    }

    @Override
    public String toString() {
        return info.toString();
    }

    public static final class ModuleInfo {

        public static final ModuleInfo EMPTY = new ModuleInfo("Unknown", "Unknown", null, "Unknown", 0);

        private final String name;
        private final String author;
        private final String url;
        private final String versionName;
        private final int buildNumber;

        public ModuleInfo(@Nonnull final String name, @Nonnull final String author, @Nullable final String url,
                          @Nonnull final String versionName, final int buildNumber) {
            this.name = name;
            this.author = author;
            this.url = url;
            this.versionName = versionName;
            this.buildNumber = buildNumber;
        }

        @Nonnull
        public String getName() {
            return name;
        }

        @Nonnull
        public String getAuthor() {
            return author;
        }

        @Nullable
        public String getUrl() {
            return url;
        }

        @Nonnull
        public String getVersionName() {
            return versionName;
        }

        public int getBuildNumber() {
            return buildNumber;
        }

        @Override
        public String toString() {
            return String.format("Module[%s v%s]", name, versionName);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof ModuleInfo
                    && ((ModuleInfo) obj).name.equalsIgnoreCase(name)
                    && ((ModuleInfo) obj).buildNumber == buildNumber;
        }
    }
}
