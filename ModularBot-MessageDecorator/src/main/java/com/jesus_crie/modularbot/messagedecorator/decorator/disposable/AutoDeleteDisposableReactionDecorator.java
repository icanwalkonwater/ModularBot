package com.jesus_crie.modularbot.messagedecorator.decorator.disposable;

import com.jesus_crie.modularbot.messagedecorator.button.DecoratorButton;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent;

import javax.annotation.Nonnull;

public abstract class AutoDeleteDisposableReactionDecorator extends DisposableReactionDecorator {

    protected AutoDeleteDisposableReactionDecorator(@Nonnull Message binding, long timeout, @Nonnull DecoratorButton... buttons) {
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
