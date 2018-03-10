package com.jesus_crie.modularbot2.utils;

import net.dv8tion.jda.core.JDA;

public class F {

    public static String f(String format, Object... args) {
        return String.format(format, args);
    }

    public static String f(JDA.ShardInfo shardInfo) {
        return f("[" + (shardInfo.getShardId() + 1) + " / " + shardInfo.getShardTotal() + "]");
    }
}
