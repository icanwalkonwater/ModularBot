package com.jesus_crie.modularbot_message_decorator.decorator.disposable;

import com.jesus_crie.modularbot_message_decorator.reaction.DecoratorButton;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageReaction;

import javax.annotation.Nonnull;

/**
 * Decorator with a single button that will delete the message when clicked or when it times out.
 */
public class AlertMessageDecorator extends DisposableMessageDecorator {

    private static final MessageReaction.ReactionEmote DEFAULT_REACTION = new MessageReaction.ReactionEmote("\u274C", null, null);
    private final boolean deleteAfter;

    public AlertMessageDecorator(@Nonnull final Message binding, final long timeout, final boolean deleteAfter) {
        this(binding, timeout, DEFAULT_REACTION, deleteAfter);
    }

    public AlertMessageDecorator(@Nonnull final Message binding, final long timeout,
                                 @Nonnull final MessageReaction.ReactionEmote emote, boolean deleteAfter) {
        super(binding, timeout);
        this.deleteAfter = deleteAfter;
        buttons.add(DecoratorButton.fromReactionEmote(emote, e -> destroy()));
    }

    @Override
    public void destroy() {
        super.destroy();
        if (deleteAfter) binding.delete().complete();
    }
}
