package com.jesus_crie.modularbot_message_decorator.decorator.disposable;

import com.jesus_crie.modularbot_message_decorator.decorator.ReactionDecorator;
import com.jesus_crie.modularbot_message_decorator.reaction.DecoratorButton;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent;

import javax.annotation.Nonnull;

/**
 * An implementation of {@link ReactionDecorator ReactionDecorator} that can be successfully triggered only one time.
 */
public abstract class DisposableMessageDecorator extends ReactionDecorator {

    public DisposableMessageDecorator(@Nonnull final Message binding, final long timeout, @Nonnull final DecoratorButton... buttons) {
        super(binding, timeout, buttons);
    }

    /**
     * Extends the basic behaviour by destroying the decorator if a button is triggered.
     *
     * @param event The event that was thrown.
     * @return True if the event has triggered a button, otherwise false.
     */
    @Override
    protected boolean onTrigger(@Nonnull GenericMessageReactionEvent event) {
        if (super.onTrigger(event)) {
            destroy();
            return true;
        }

        return false;
    }
}
