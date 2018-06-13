package com.jesus_crie.modularbot.utils;

import net.dv8tion.jda.core.JDA;

import javax.annotation.Nonnull;

public class F {

    public static String f(String format, Object... args) {
        return String.format(format, args);
    }

    public static String f(JDA.ShardInfo shardInfo) {
        return f("[" + (shardInfo.getShardId() + 1) + " / " + shardInfo.getShardTotal() + "]");
    }

    public static String fullClassToSimpleClassName(@Nonnull String fullClassName) {
        if (!fullClassName.contains(".")) {
            return fullClassName;
        } else {
            String[] parts = fullClassName.split(".");
            return parts[parts.length - 1];
        }
    }
}
