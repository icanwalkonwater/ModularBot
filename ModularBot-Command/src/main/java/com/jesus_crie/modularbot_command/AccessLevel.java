package com.jesus_crie.modularbot_command;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;

import javax.annotation.Nonnull;
import java.util.EnumSet;

public class  AccessLevel {

    /**
     * Your id goes here, here is mine but you can change it.
     */
    public static long CREATOR_ID = 0;

    /**
     * Literally everyone, everywhere.
     */
    public static final AccessLevel EVERYONE = new AccessLevel(
            EnumSet.noneOf(Permission.class), false, false, 0);

    /**
     * Only available in guilds.
     */
    public static final AccessLevel GUILD_ONLY = new AccessLevel(
            EnumSet.noneOf(Permission.class), true, false, 0);

    /**
     * Only available in private channels.
     */
    public static final AccessLevel PRIVATE_ONLY = new AccessLevel(
            EnumSet.noneOf(Permission.class), false, true, 0);

    /**
     * In guilds, peoples who can delete messages.
     */
    public static final AccessLevel GUILD_MODERATOR = new AccessLevel(
            EnumSet.of(Permission.MESSAGE_MANAGE), true, false, 0);

    /**
     * In guilds, those who have all of the permissions.
     */
    public static final AccessLevel GUILD_ADMINISTRATOR = new AccessLevel(
            EnumSet.of(Permission.ADMINISTRATOR), true, false, 0);

    /**
     * Only the creator of the bot.
     */
    public static final AccessLevel CREATOR = new AccessLevel(EnumSet.noneOf(Permission.class), false, false, CREATOR_ID);

    private final EnumSet<Permission> requiredPermissions;
    private final boolean onlyGuild;
    private final boolean onlyPrivate;
    private final long user;

    /**
     * @param requiredPermissions   The permissions required if in a guild.
     * @param onlyGuild             Flag that indicate that this can only occur in a guild.
     * @param onlyPrivate           Flag that indicate that this can only occur in a private channel.
     * @param user                  Flag that indicate that only this user can use this, /!\ overwrite every other flag /!\.
     */
    public AccessLevel(@Nonnull EnumSet<Permission> requiredPermissions, boolean onlyGuild, boolean onlyPrivate, long user) {
        this.requiredPermissions = requiredPermissions;
        this.onlyGuild = onlyGuild;
        this.onlyPrivate = onlyPrivate;
        this.user = user;
    }

    /**
     * The logic behind
     * @param event
     * @return
     */
    public boolean check(@Nonnull CommandEvent event) {
        // Check user
        if (user != 0)
            return user == event.getAuthor().getIdLong();

        // If in a guild
        if (event.isFromType(ChannelType.TEXT)) {
            // Only private, eliminatory
            if (onlyPrivate) return false;
            // If there are required permissions
            if (requiredPermissions.size() != 0) {
                return event.getMember().hasPermission(event.getTextChannel(), requiredPermissions);
            }
            return true;

        // If in a private channel
        } else if (event.isFromType(ChannelType.PRIVATE)) {
            // The only criteria is the only guild flag
            return !onlyGuild;

        // An unknown type of channel ? Abort in case of
        } else {
            return false;
        }
    }
}
