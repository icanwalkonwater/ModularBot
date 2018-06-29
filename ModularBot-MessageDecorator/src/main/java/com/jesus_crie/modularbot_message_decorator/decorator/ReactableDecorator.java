package com.jesus_crie.modularbot_message_decorator.decorator;

import com.jesus_crie.modularbot_message_decorator.reaction.DecoratorButton;
import net.dv8tion.jda.core.entities.Message;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

public abstract class ReactableDecorator extends MessageDecorator {

    protected final List<DecoratorButton> buttons;

    protected ReactableDecorator(@Nonnull final Message binding, final long timeout, @Nonnull final DecoratorButton... buttons) {
        super(binding, timeout);
        this.buttons = Arrays.asList(buttons);
    }
}
