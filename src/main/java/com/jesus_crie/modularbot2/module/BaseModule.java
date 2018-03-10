package com.jesus_crie.modularbot2.module;

import com.jesus_crie.modularbot2.ModularBot;
import com.jesus_crie.modularbot2.ModularBotBuilder;
import net.dv8tion.jda.core.JDA;

import javax.annotation.Nonnull;
import java.util.List;

public abstract class BaseModule {

    protected ModuleInfo info;

    protected BaseModule(@Nonnull ModuleInfo i) {
        info = i;
    }

    public ModuleInfo getInfo() {
        return info;
    }

    public void onLoad() {}

    public void onPreInitialization(@Nonnull ModularBotBuilder builder) {}

    public void onPostInitialization(@Nonnull ModularBot bot) {}

    public void onPrePrepareShards(@Nonnull ModularBot bot, int shardTotal) {}

    public void onPostShardLoaded(@Nonnull ModularBot bot, List<JDA> shards) {}

    public void onUnload() {}

    public final class ModuleInfo {

        private final String name;
        private final String author;
        private final String url;
        private final String versionName;
        private final int buildNumber;

        public ModuleInfo(String name, String author, String url, String versionName, int buildNumber) {
            this.name = name;
            this.author = author;
            this.url = url;
            this.versionName = versionName;
            this.buildNumber = buildNumber;
        }

        public String getName() {
            return name;
        }

        public String getAuthor() {
            return author;
        }

        public String getUrl() {
            return url;
        }

        public String getVersionName() {
            return versionName;
        }

        public int getBuildNumber() {
            return buildNumber;
        }

        @Override
        public String toString() {
            return name + "#" + buildNumber;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof ModuleInfo && ((ModuleInfo) obj).name.equalsIgnoreCase(name);
        }
    }
}
