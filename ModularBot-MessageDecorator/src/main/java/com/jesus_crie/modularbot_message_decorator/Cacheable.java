package com.jesus_crie.modularbot_message_decorator;

import com.electronwill.nightconfig.core.Config;
import com.jesus_crie.modularbot.ModularBot;
import com.jesus_crie.modularbot_message_decorator.button.DecoratorButton;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageReaction;

import javax.annotation.Nonnull;
import java.util.Optional;

public interface Cacheable {

    // Constants, accessible from subclasses //

    String KEY_CLASS = "_class";
    String KEY_BINDING_ID = "binding_id";
    String KEY_TIMEOUT = "timeout";
    String KEY_ACTION_FUNCTIONAL = "action_functional";
    String KEY_ACTION_SCRIPT = "action_js";
    String KEY_BUTTONS = "buttons";
    String KEY_DELETE_AFTER = "delete_after";
    String KEY_EMOTE = "emote";
    String KEY_BUTTON_YES = "button_yes";
    String KEY_BUTTON_NO = "button_no";
    String KEY_TIMEOUT_ACTION = "timeout_action";
    String KEY_BINDING_CHANNEL_ID = "binding_channel_id";
    String KEY_POLL_VOTES = "poll_votes";
    String KEY_POLL_VOTE_ACTION = "poll_vote_action";

    /**
     * Used to serialize the objet into a {@link Config Config} usable by the config module.
     *
     * @return A {@link Config Config} containing the information necessary to restore the object.
     */
    @Nonnull
    Config serialize();

    /**
     * Retrieve the binding from discord using the channel and binding id.
     *
     * @param chanId    The id if the channel.
     * @param bindingId The id of the bound message.
     * @param bot       The current instance of {@link ModularBot ModularBot}.
     * @return The bound message.
     * @throws IllegalStateException If the channel nor the message can't be found (probably deleted).
     */
    @Nonnull
    static Message getBinding(final long chanId, final long bindingId, @Nonnull final ModularBot bot) {
        final MessageChannel channel = Optional.ofNullable((MessageChannel) bot.getTextChannelById(chanId))
                .orElseGet(() -> bot.getPrivateChannelById(chanId));
        if (channel == null)
            throw new IllegalStateException("The bound channel can't be found !");

        return Optional.ofNullable(channel.getMessageById(bindingId).complete())
                .orElseThrow(() -> new IllegalStateException("The bound message can't be found !"));
    }

    /**
     * Deserialize an emote serialized by {@link DecoratorButton#getEmoteSerialized()}
     *
     * @param serializedEmote A string representing the serialized emote.
     * @param bot             The current instance of {@link ModularBot ModularBot}.
     * @return The corresponding {@link net.dv8tion.jda.core.entities.MessageReaction.ReactionEmote ReactionEmote}.
     */
    @Nonnull
    static MessageReaction.ReactionEmote deserializeReactionEmote(@Nonnull final String serializedEmote, @Nonnull final ModularBot bot) {
        final Emote emote = bot.getEmoteById(serializedEmote);
        return emote == null
                ? new MessageReaction.ReactionEmote(serializedEmote, null, null)
                : new MessageReaction.ReactionEmote(emote);
    }
}
