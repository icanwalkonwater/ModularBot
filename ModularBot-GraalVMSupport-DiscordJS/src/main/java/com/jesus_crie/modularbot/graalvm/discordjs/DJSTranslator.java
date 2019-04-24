package com.jesus_crie.modularbot.graalvm.discordjs;

import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.ChannelType;

import javax.annotation.Nonnull;

public final class DJSTranslator {

    @Nonnull
    public static String fromChannelType(@Nonnull final ChannelType type) {
        switch (type) {
            case PRIVATE:
                return "dm";
            case GROUP:
                return "group";
            case TEXT:
                return "text";
            case VOICE:
                return "voice";
            case CATEGORY:
                return "category";
            default:
                return "";
        }
    }

    @Nonnull
    public static ChannelType toChannelType(@Nonnull final String type) {
        switch (type) {
            case "dm":
                return ChannelType.PRIVATE;
            case "group":
                return ChannelType.GROUP;
            case "text":
                return ChannelType.TEXT;
            case "voice":
                return ChannelType.VOICE;
            case "category":
                return ChannelType.CATEGORY;
            default:
                return ChannelType.UNKNOWN;
        }
    }

    @Nonnull
    public static String fromStatus(@Nonnull final OnlineStatus status) {
        switch (status) {
            case ONLINE:
                return "online";
            case IDLE:
                return "idle";
            case INVISIBLE:
                return "invisible";
            case DO_NOT_DISTURB:
                return "dnd";
            default:
                return "";
        }
    }

    @Nonnull
    public static OnlineStatus toStatus(@Nonnull final String status) {
        switch (status) {
            case "online":
                return OnlineStatus.ONLINE;
            case "idle":
                return OnlineStatus.IDLE;
            case "invisible":
                return OnlineStatus.INVISIBLE;
            case "dnd":
                return OnlineStatus.DO_NOT_DISTURB;
            default:
                return OnlineStatus.UNKNOWN;
        }
    }
}
