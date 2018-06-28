package com.jesus_crie.modularbot_message_decorator.decorator;

import com.jesus_crie.modularbot.utils.Waiter;
import com.jesus_crie.modularbot_message_decorator.reaction.DecoratorButton;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public abstract class MessageDecorator {

    protected Message bindTo;
    protected final User target;
    protected final long timeout;
    protected final Map<String, DecoratorButton> buttons = new HashMap<>();
    protected Waiter.WaiterListener<? extends GenericMessageReactionEvent> listener = Waiter.WaiterListener.EMPTY;
    /*protected DecoratorListener callback = null;*/

    protected boolean isAlive = true;

    protected MessageDecorator(@Nonnull final Message bindTo, @Nullable final User target, final long timeout, @Nonnull final DecoratorButton... buttons) {
        this.bindTo = bindTo;
        this.target = target;
        this.timeout = timeout;

        for (DecoratorButton button : buttons) {
            // TODO 28/06/2018 things
        }
    }
}
