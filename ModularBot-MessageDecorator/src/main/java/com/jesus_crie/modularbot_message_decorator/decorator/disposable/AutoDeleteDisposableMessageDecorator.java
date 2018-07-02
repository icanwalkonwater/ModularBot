package com.jesus_crie.modularbot_message_decorator.decorator.disposable;

import com.jesus_crie.modularbot_message_decorator.reaction.DecoratorButton;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent;

import javax.annotation.Nonnull;

public abstract class AutoDeleteDisposableMessageDecorator extends DisposableMessageDecorator {

    protected AutoDeleteDisposableMessageDecorator(@Nonnull Message binding, long timeout, @Nonnull DecoratorButton... buttons) {
        super(binding, timeout, buttons);
    }

    @Override
    protected boolean onTrigger(@Nonnull GenericMessageReactionEvent event) {
        if (super.onTrigger(event)) {
            binding.delete().complete();
            return true;
        }
        return false;
    }
}
