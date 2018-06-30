package com.jesus_crie.modularbot_message_decorator.decorator;

import com.jesus_crie.modularbot.utils.Waiter;
import com.jesus_crie.modularbot_message_decorator.reaction.DecoratorButton;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Decorator made to interact with the reactions of a message.
 */
public abstract class ReactionDecorator extends MessageDecorator<GenericMessageReactionEvent> {

    protected final List<DecoratorButton> buttons = new ArrayList<>();

    /**
     * Initialize the decorator.
     *
     * @param binding    The bound message.
     * @param timeout    The amount of milliseconds before this decorator expire or 0 for infinite.
     * @param buttons    The buttons (reaction) that can trigger this decorator.
     * @see MessageDecorator
     */
    protected ReactionDecorator(@Nonnull final Message binding, final long timeout, @Nonnull final DecoratorButton... buttons) {
        super(binding, timeout);
        Collections.addAll(this.buttons, buttons);
        listener = createListener();
    }

    /**
     * {@inheritDoc}
     * Initialize each button and then register the waiter.
     */
    @Override
    public void setup() {
        buttons.forEach(b -> b.setupEmote(binding).complete());
        listener.register();
    }

    @Override
    protected Waiter.WaiterListener<GenericMessageReactionEvent> createListener(@Nonnull Object... args) {
        return Waiter.createListener(binding.getJDA(),
                GenericMessageReactionEvent.class,
                event -> event.getMessageIdLong() == binding.getIdLong(),
                this::onTrigger,
                this::onTimeout,
                timeout,
                false);
    }

    /**
     * Called when the decorator is triggered.
     *
     * @param event The event that was thrown.
     * @return True if the event has triggered a button, otherwise false.
     */
    protected boolean onTrigger(@Nonnull final GenericMessageReactionEvent event) {
        for (DecoratorButton button : buttons)
            if (button.onTrigger(event)) return true;

        return false;
    }

    @Override
    protected void onTimeout() {
        destroy();
    }

    @Override
    public void destroy() {
        listener.unregister();
        isAlive = false;
    }
}
