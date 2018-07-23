package com.jesus_crie.modularbot_message_decorator.decorator.disposable;

import com.electronwill.nightconfig.core.Config;
import com.jesus_crie.modularbot.ModularBot;
import com.jesus_crie.modularbot_message_decorator.Cacheable;
import com.jesus_crie.modularbot_message_decorator.button.DecoratorButton;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageReaction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
     * @param serialized The serialized version of the decorator.
     * @param bot    The current instance of the bot, used to retrieve the binding.
     * @return The deserialized {@link AlertReactionDecorator AlertReactionDecorator} or {@code null} if it failed.
     * @throws IllegalArgumentException If a required field is missing.
     * @throws IllegalStateException    If the bound message no longer exists.
     */
    @Nullable
    public static AlertReactionDecorator tryDeserialize(@Nonnull final Config serialized, @Nonnull final ModularBot bot) {
        // Assuming that the KEY_CLASS field is correct.

        final Long chanId = serialized.get(KEY_BINDING_CHANNEL_ID);
        final Long bindingId = serialized.get(KEY_BINDING_ID);
        final long expireTime = serialized.<Number>getOrElse(KEY_TIMEOUT, 1).longValue(); // 1 will be an expired decorator.
        final boolean deleteAfter = serialized.getOrElse(KEY_DELETE_AFTER, false);
        final String emoteSerialized = serialized.get(KEY_EMOTE);

        // Check the essential fields.
        if (chanId == null || bindingId == null)
            throw new IllegalArgumentException("One or more fields are missing !");

        // Check if expired, should never happen but who knows ?
        if (expireTime < 0)
            throw new IllegalStateException("Trying to deserialize a decorator that is marked as expired ! (timeout < 0)");

        // Retrieve the bound message.
        final Message binding = Cacheable.getBinding(chanId, bindingId, bot);

        final long timeout = expireTime == 0 ? 0 : expireTime - System.currentTimeMillis();

        // Retrieve the emote if set, otherwise default emote.
        final MessageReaction.ReactionEmote rEmote = emoteSerialized != null
                ? Cacheable.deserializeReactionEmote(emoteSerialized, bot)
                : DEFAULT_REACTION;

        try {
            // Build that.
            return new AlertReactionDecorator(binding,
                    timeout,
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
        // Check if alive, should never trigger because it's checked but who knows.
        if (!isAlive)
            throw new IllegalStateException("Can't serialize an expired decorator !");

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
