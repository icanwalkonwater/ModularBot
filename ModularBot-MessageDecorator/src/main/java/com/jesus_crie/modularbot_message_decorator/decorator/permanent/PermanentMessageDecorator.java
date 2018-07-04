package com.jesus_crie.modularbot_message_decorator.decorator.permanent;

import com.jesus_crie.modularbot_message_decorator.button.DecoratorButton;
import com.jesus_crie.modularbot_message_decorator.decorator.ReactionDecorator;
import net.dv8tion.jda.core.entities.Message;

import javax.annotation.Nonnull;

/**
 * Mark the decorator as permanent.
 * Has no real effect, it's just a {@link ReactionDecorator ReactionDecorator}.
 */
public abstract class PermanentMessageDecorator extends ReactionDecorator {

    protected PermanentMessageDecorator(@Nonnull Message binding, long timeout, @Nonnull DecoratorButton... buttons) {
        super(binding, timeout, buttons);
    }
}
