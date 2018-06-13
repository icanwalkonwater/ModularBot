package com.jesus_crie.modularbot.module;

import com.jesus_crie.modularbot.ModularBot;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class BaseModule implements Lifecycle {

    /**
     * Package private field, describe the state of the module.
     */
    Lifecycle.State state = State.STOPPED;

    protected final ModuleInfo info;

    /**
     * Reference to the instance of {@link ModularBot}.
     * Only available if state {@link Lifecycle.State#STARTED} has been reached and {@link #onShardsReady(ModularBot)}
     * has been called.
     */
    protected ModularBot bot;

    protected BaseModule(final @Nonnull ModuleInfo i) {
        info = i;
    }

    @Override
    public void onShardsReady(@Nonnull ModularBot bot) {
        this.bot = bot;
    }

    public ModuleInfo getInfo() {
        return info;
    }

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
            return "Module[" + name + "#" + versionName + "]";
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof ModuleInfo && ((ModuleInfo) obj).name.equalsIgnoreCase(name);
        }
    }
}
