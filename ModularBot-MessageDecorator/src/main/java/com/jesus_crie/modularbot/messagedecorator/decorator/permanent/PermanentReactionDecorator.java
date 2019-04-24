package com.jesus_crie.modularbot.messagedecorator.decorator.permanent;

import com.jesus_crie.modularbot.messagedecorator.button.DecoratorButton;
import com.jesus_crie.modularbot.messagedecorator.decorator.ReactionDecorator;
import net.dv8tion.jda.core.entities.Message;

import javax.annotation.Nonnull;

/**
 * Mark the decorator as permanent.
 * Has no real effect, it's just a {@link ReactionDecorator ReactionDecorator}.
 */
public abstract class PermanentReactionDecorator extends ReactionDecorator {

    protected PermanentReactionDecorator(@Nonnull Message binding, long timeout, @Nonnull DecoratorButton... buttons) {
        super(binding, timeout, buttons);
    }
}
