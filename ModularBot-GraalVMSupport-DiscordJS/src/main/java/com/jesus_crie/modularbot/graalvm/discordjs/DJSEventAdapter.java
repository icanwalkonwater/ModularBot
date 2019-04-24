package com.jesus_crie.modularbot.graalvm.discordjs;

import net.dv8tion.jda.client.events.group.GroupJoinEvent;
import net.dv8tion.jda.client.events.group.GroupLeaveEvent;
import net.dv8tion.jda.client.events.group.update.GenericGroupUpdateEvent;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.*;
import net.dv8tion.jda.core.events.channel.category.CategoryCreateEvent;
import net.dv8tion.jda.core.events.channel.category.CategoryDeleteEvent;
import net.dv8tion.jda.core.events.channel.category.update.GenericCategoryUpdateEvent;
import net.dv8tion.jda.core.events.channel.priv.PrivateChannelCreateEvent;
import net.dv8tion.jda.core.events.channel.priv.PrivateChannelDeleteEvent;
import net.dv8tion.jda.core.events.channel.text.TextChannelCreateEvent;
import net.dv8tion.jda.core.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.core.events.channel.text.update.GenericTextChannelUpdateEvent;
import net.dv8tion.jda.core.events.channel.voice.VoiceChannelCreateEvent;
import net.dv8tion.jda.core.events.channel.voice.VoiceChannelDeleteEvent;
import net.dv8tion.jda.core.events.channel.voice.update.GenericVoiceChannelUpdateEvent;
import net.dv8tion.jda.core.events.emote.EmoteAddedEvent;
import net.dv8tion.jda.core.events.emote.EmoteRemovedEvent;
import net.dv8tion.jda.core.events.emote.update.GenericEmoteUpdateEvent;
import net.dv8tion.jda.core.events.guild.*;
import net.dv8tion.jda.core.events.guild.member.GenericGuildMemberEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.events.guild.update.GenericGuildUpdateEvent;
import net.dv8tion.jda.core.events.guild.voice.GenericGuildVoiceEvent;
import net.dv8tion.jda.core.events.message.MessageBulkDeleteEvent;
import net.dv8tion.jda.core.events.message.MessageDeleteEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.MessageUpdateEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionRemoveAllEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.core.events.role.RoleCreateEvent;
import net.dv8tion.jda.core.events.role.RoleDeleteEvent;
import net.dv8tion.jda.core.events.role.update.GenericRoleUpdateEvent;
import net.dv8tion.jda.core.events.user.UserTypingEvent;
import net.dv8tion.jda.core.events.user.update.GenericUserPresenceEvent;
import net.dv8tion.jda.core.events.user.update.GenericUserUpdateEvent;
import net.dv8tion.jda.core.hooks.EventListener;
import org.graalvm.polyglot.Value;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;

public class DJSEventAdapter implements EventListener {

    private Value eventEmitter;

    @Override
    public void onEvent(@Nonnull final Event event) {
        String eventName = event.getClass().getSimpleName();
        eventName = Character.toLowerCase(eventName.charAt(0)) + eventName.substring(1);

        eventEmitter.invokeMember("emit", eventName, event);

        // Emulate channelCreate
        if (event instanceof TextChannelCreateEvent
                || event instanceof VoiceChannelCreateEvent
                || event instanceof CategoryCreateEvent
                || event instanceof PrivateChannelCreateEvent
                || event instanceof GroupJoinEvent) {
            emit("channelCreate", extractChannel(event));
        }

        // Emulate channelDelete
        else if (event instanceof TextChannelDeleteEvent
                || event instanceof VoiceChannelDeleteEvent
                || event instanceof CategoryDeleteEvent
                || event instanceof PrivateChannelDeleteEvent
                || event instanceof GroupLeaveEvent) {
            emit("channelDelete", extractChannel(event));
        }

        // Emulate channelPinsUpdate
        // Not supported

        // Emulate channelUpdate
        // /!\ No distinction between old and new value
        else if (event instanceof GenericTextChannelUpdateEvent
                || event instanceof GenericVoiceChannelUpdateEvent
                || event instanceof GenericCategoryUpdateEvent
                || event instanceof GenericGroupUpdateEvent) {
            final Channel channel = (Channel) ((UpdateEvent) event).getEntity();
            emit("channelUpdate", channel, channel);
        }

        // Emulate clientUserGuildSettingsUpdate
        // Not supported

        // Emulate clientUserSettingsUpdate
        // Not supported (client only feature)

        // Emulate debug
        // No logger messages are supposed to be catch by the module

        // Emulate disconnect
        else if (event instanceof DisconnectEvent) {
            emit("disconnect", ((DisconnectEvent) event).getCloseCode());
        }

        // Emulate emojiCreate
        else if (event instanceof EmoteAddedEvent) {
            emit("emojiCreate", ((EmoteAddedEvent) event).getEmote());
        }

        // Emulate emojiDelete
        else if (event instanceof EmoteRemovedEvent) {
            emit("emojiDelete", ((EmoteRemovedEvent) event).getEmote());
        }

        // Emulate emojiUpdate
        // No distinction between old and new
        else if (event instanceof GenericEmoteUpdateEvent) {
            final Emote emote = ((GenericEmoteUpdateEvent) event).getEmote();
            emit("emojiUpdate", emote, emote);
        }

        // Emulate error
        // No logger messages supported

        // Emulate guildBanAdd
        else if (event instanceof GuildBanEvent) {
            emit("guildBanAdd", ((GuildBanEvent) event).getGuild(), ((GuildBanEvent) event).getUser());
        }

        // Emulate guildBanRemove
        else if (event instanceof GuildUnbanEvent) {
            emit("guildBanRemove", ((GuildUnbanEvent) event).getGuild(), ((GuildUnbanEvent) event).getUser());
        }

        // Emulate guildCreate
        else if (event instanceof GuildJoinEvent) {
            emit("guildCreate", ((GuildJoinEvent) event).getGuild());
        }

        // Emulate guildDelete
        else if (event instanceof GuildLeaveEvent) {
            emit("guildDelete", ((GuildLeaveEvent) event).getGuild());
        }

        // Emulate guildMemberAdd
        else if (event instanceof GuildMemberJoinEvent) {
            emit("guildMemberAdd", ((GuildMemberJoinEvent) event).getMember());
        }

        // Emulate guildMemberAvailable
        // Not exposed by JDA

        // Emulate guildMemberRemove
        else if (event instanceof GuildMemberLeaveEvent) {
            emit("guildMemberRemove", ((GuildMemberLeaveEvent) event).getMember());
        }

        // Emulate guildMembersChunk
        // Not exposed by JDA

        // Emulate guildMemberSpeaking
        // Not available

        // Emulate guildMemberUpdate
        // No distinction between old and new
        else if (event instanceof GenericGuildMemberEvent) {
            final Member member = ((GenericGuildMemberEvent) event).getMember();
            emit("guildMemberUpdate", member, member);
        }

        // Emulate guildUnavailable
        else if (event instanceof GuildUnavailableEvent) {
            emit("guildUnavailable", ((GuildUnavailableEvent) event).getGuild());
        }

        // Emulate guildUpdate
        else if (event instanceof GenericGuildUpdateEvent) {
            final Guild guild = ((GenericGuildUpdateEvent) event).getGuild();
            emit("guildUpdate", guild, guild);
        }

        // Emulate message
        else if (event instanceof MessageReceivedEvent) {
            emit("message", ((MessageReceivedEvent) event).getMessage());
        }

        // Emulate messageDelete
        // Only message id
        else if (event instanceof MessageDeleteEvent) {
            emit("messageDelete", ((MessageDeleteEvent) event).getMessageId());
        }

        // Emulate messageDeleteBulk
        // Only message ids
        else if (event instanceof MessageBulkDeleteEvent) {
            emit("messageDeleteBulk", ((MessageBulkDeleteEvent) event).getMessageIds());
        }

        // Emulate messageReactionAdd
        else if (event instanceof MessageReactionAddEvent) {
            emit("messageReactionAdd", ((MessageReactionAddEvent) event).getReaction(), ((MessageReactionAddEvent) event).getUser());
        }

        // Emulate messageReactionRemove
        else if (event instanceof MessageReactionRemoveEvent) {
            emit("messageReactionRemove", ((MessageReactionRemoveEvent) event).getReaction(), ((MessageReactionRemoveEvent) event).getUser());
        }

        // Emulate messageReactionRemoveAll
        // Only message id
        else if (event instanceof MessageReactionRemoveAllEvent) {
            emit("messageReactionRemoveAll", ((MessageReactionRemoveAllEvent) event).getMessageId());
        }

        // Emulate messageUpdate
        else if (event instanceof MessageUpdateEvent) {
            final Message message = ((MessageUpdateEvent) event).getMessage();
            emit("messageUpdate", message, message);
        }

        // Emulate presenceUpdate
        else if (event instanceof GenericUserPresenceEvent) {
            final Member member = ((GenericUserPresenceEvent) event).getMember();
            emit("presenceUpdate", member);
        }

        // Emulate rateLimit
        // Handled by JDA

        // Emulate ready
        else if (event instanceof ReadyEvent) {
            emit("ready");
        }

        // Emulate reconnecting
        else if (event instanceof ReconnectedEvent) {
            emit("reconnecting");
        }

        // Emulate resume
        // No info on how many events where resumed
        else if (event instanceof ResumedEvent) {
            emit("resume", null);
        }

        // Emulate roleCreate
        else if (event instanceof RoleCreateEvent) {
            emit("roleCreate", ((RoleCreateEvent) event).getRole());
        }

        // Emulate roleDelete
        else if (event instanceof RoleDeleteEvent) {
            emit("roleDelete", ((RoleDeleteEvent) event).getRole());
        }

        // Emulate roleUpdate
        else if (event instanceof GenericRoleUpdateEvent) {
            final Role role = ((GenericRoleUpdateEvent) event).getRole();
            emit("roleUpdate", role, role);
        }

        // Emulate typingStart
        else if (event instanceof UserTypingEvent) {
            emit("typingStart", ((UserTypingEvent) event).getChannel(), ((UserTypingEvent) event).getUser());
        }

        // Emulate typingStop
        // Not handled by JDA

        // Emulate userNoteUpdate
        // No client feature supported

        // Emulate userUpdate
        // No distinction between old and new
        else if (event instanceof GenericUserUpdateEvent) {
            final User user = ((GenericUserUpdateEvent) event).getUser();
            emit("userUpdate", user, user);
        }

        // Emulate voiceStateUpdate
        // No distinction between old and new
        else if (event instanceof GenericGuildVoiceEvent) {
            final Member member = ((GenericGuildVoiceEvent) event).getMember();
            emit("voiceStateUpdate", member, member);
        }

        // Emulate warn
        // No log handled here
    }

    private void emit(@Nonnull final Object... args) {
        eventEmitter.invokeMember("emit", args);
    }

    /**
     * Try to extract a channel (or category) from a raw event.
     *
     * @param event - The event to extract from.
     * @return The extracted {@link Channel}.
     */
    @Nonnull
    private Channel extractChannel(Event event) {
        try {
            return (Channel) event.getClass().getMethod("getChannel").invoke(event);
        } catch (NoSuchMethodException e) {
            try {
                return (Channel) event.getClass().getMethod("getCategory").invoke(event);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e1) {
                throw new IllegalArgumentException(event + " is not a channel related event !");
            }

        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException(event + " is not a channel related event !");
        }
    }
}
