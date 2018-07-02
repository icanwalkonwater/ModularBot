package com.jesus_crie.modularbot_message_decorator.decorator.disposable;

import com.jesus_crie.modularbot_message_decorator.reaction.DecoratorButton;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageReaction;

import javax.annotation.Nonnull;

/**
 * Decorator with a single button that will delete the message when clicked or when it times out.
 * Made for informative messages that can be dismissed.
 */
public class AlertMessageDecorator extends SafeAutoDestroyDisposableMessageDecorator {

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
    public AlertMessageDecorator(@Nonnull final Message binding, final long timeout, final boolean deleteAfter) {
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
    public AlertMessageDecorator(@Nonnull final Message binding, final long timeout,
                                 @Nonnull final MessageReaction.ReactionEmote emote, boolean deleteAfter) {
        super(binding, timeout, deleteAfter);
        buttons.add(DecoratorButton.fromReactionEmote(emote, null));
    }
}
