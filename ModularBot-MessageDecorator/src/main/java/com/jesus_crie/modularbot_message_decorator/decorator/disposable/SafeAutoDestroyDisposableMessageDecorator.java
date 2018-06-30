package com.jesus_crie.modularbot_message_decorator.decorator.disposable;

import com.jesus_crie.modularbot_message_decorator.reaction.DecoratorButton;
import net.dv8tion.jda.core.entities.Message;

import javax.annotation.Nonnull;

/**
 * A disposable decorator that can auto destroy after being triggered.
 * If it's not set to auto destroy, it will simply remove the buttons.
 */
public abstract class SafeAutoDestroyDisposableMessageDecorator extends DisposableMessageDecorator {

    private final boolean deleteAfter;

    /**
     * Create an auto destroy disposable decorator.
     *
     * @param binding     The bound message.
     * @param timeout     The amount of milliseconds before this decorator expire or 0 for infinite.
     * @param deleteAfter Whether or not to delete the bound message after.
     * @param buttons     The buttons (reaction) that can trigger this decorator.
     */
    protected SafeAutoDestroyDisposableMessageDecorator(@Nonnull final Message binding, final long timeout,
                                                        final boolean deleteAfter, @Nonnull final DecoratorButton... buttons) {
        super(binding, timeout, buttons);
        this.deleteAfter = deleteAfter;
    }

    @Override
    public void destroy() {
        super.destroy();
        if (deleteAfter) binding.delete().complete();
        else destroyButtons();
    }
}
