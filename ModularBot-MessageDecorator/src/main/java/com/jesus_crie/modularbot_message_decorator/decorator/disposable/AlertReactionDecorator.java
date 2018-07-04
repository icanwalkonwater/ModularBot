package com.jesus_crie.modularbot_message_decorator.decorator.disposable;

import com.electronwill.nightconfig.core.Config;
import com.jesus_crie.modularbot.ModularBot;
import com.jesus_crie.modularbot_message_decorator.Cacheable;
import com.jesus_crie.modularbot_message_decorator.button.DecoratorButton;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageReaction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Decorator with a single button that will delete the message when clicked or when it times out.
 * Made for informative messages that can be dismissed.
 */
public class AlertReactionDecorator extends SafeAutoDestroyDisposableReactionDecorator implements Cacheable {

    /**
     * The default emote. Correspond to the unicde character "❌".
     */
    public static final MessageReaction.ReactionEmote DEFAULT_REACTION = new MessageReaction.ReactionEmote("\u274C", null, null);

    /**
     * Create an alert decorator with the default emote.
     *
     * @param binding     The bound message.
     * @param timeout     The amount of time in milliseconds before the decorator times out, or 0 for infinite.
     * @param deleteAfter Whether the message should be deleted when the decorator is being destroyed.
     */
    public AlertReactionDecorator(@Nonnull final Message binding, final long timeout, final boolean deleteAfter) {
        this(binding, timeout, DEFAULT_REACTION, deleteAfter);
    }

    /**
     * Create an alert decorator with a custom emote.
     *
     * @param binding     The bound message.
     * @param timeout     The amount of time in milliseconds before the decorator times out, or 0 for infinite.
     * @param emote       The emote to use.
     * @param deleteAfter Whether the message should be deleted when the decorator is being destroyed.
     */
    public AlertReactionDecorator(@Nonnull final Message binding, final long timeout,
                                  @Nonnull final MessageReaction.ReactionEmote emote, boolean deleteAfter) {
        super(binding, timeout, deleteAfter);
        buttons.add(DecoratorButton.fromReactionEmote(emote, null));
    }

    /**
     * Deserialize an {@link AlertReactionDecorator AlertReactionDecorator} from a config object.
     *
     * @param config
     * @param bot
     * @return
     */
    @Nullable
    public static AlertReactionDecorator tryDeserialize(@Nonnull final Config config, @Nonnull final ModularBot bot) {
        // Assuming that the KEY_CLASS field is correct.

        final Long chanId = config.get(KEY_BINDING_CHANNEL_ID);
        final Long bindingId = config.get(KEY_BINDING_ID);
        final Long expireTime = config.get(KEY_TIMEOUT);
        final boolean deleteAfter = config.getOrElse(KEY_DELETE_AFTER, false);
        final String emoteSerialized = config.get(KEY_EMOTE);

        // Check the essential fields.
        if (chanId == null || bindingId == null || expireTime == null)
            throw new IllegalArgumentException("One or more field are missing !");

        // Retrieve the bound message.
        final MessageChannel channel = Optional.ofNullable((MessageChannel) bot.getTextChannelById(chanId))
                .orElseGet(() -> bot.getPrivateChannelById(chanId));
        final Message binding = Optional.ofNullable(channel.getMessageById(bindingId).complete())
                .orElseThrow(() -> new IllegalStateException("The bound message no longer exist !"));

        // Retrieve the emote if set, otherwise default emote.
        final MessageReaction.ReactionEmote rEmote;
        if (emoteSerialized != null) {
            final Emote emote = bot.getEmoteById(emoteSerialized);
            if (emote != null)
                rEmote = new MessageReaction.ReactionEmote(emote);
            else rEmote = new MessageReaction.ReactionEmote(emoteSerialized, null, null);
        } else rEmote = DEFAULT_REACTION;

        try {
            // Build that.
            return new AlertReactionDecorator(binding,
                    expireTime - System.currentTimeMillis(),
                    rEmote,
                    deleteAfter);
        } catch (IllegalArgumentException expected) {
            // The timeout is invalid, clear the message or destroy it.
            if (deleteAfter) binding.delete().complete();
            else binding.clearReactions().complete();

            return null;
        }
    }

    @Nonnull
    @Override
    public Config serialize() {
        final Config serialized = Config.inMemory();
        serialized.set(KEY_CLASS, getClass().getName());
        serialized.set(KEY_BINDING_CHANNEL_ID, binding.getChannel().getIdLong());
        serialized.set(KEY_BINDING_ID, binding.getIdLong());
        serialized.set(KEY_TIMEOUT, getExpireTime());
        serialized.set(KEY_DELETE_AFTER, deleteAfter);
        if (!buttons.get(0).getEmoteSerialized().equals(DEFAULT_REACTION.getName()))
            serialized.set(KEY_EMOTE, buttons.get(0).getEmoteSerialized());

        return serialized;
    }
}
