package com.jesus_crie.modularbot_message_decorator.reaction;

import com.electronwill.nightconfig.core.Config;
import com.jesus_crie.modularbot.utils.SerializationUtils;
import com.jesus_crie.modularbot_message_decorator.Cacheable;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.core.requests.RestAction;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.function.Consumer;

public abstract class DecoratorButton implements Cacheable {

    protected final Consumer<GenericMessageReactionEvent> onTrigger;

    protected DecoratorButton(@Nonnull final Consumer<GenericMessageReactionEvent> onTrigger) {
        this.onTrigger = onTrigger;
    }

    /**
     * Called when the decorator is initializing.
     * Used to add the desired emote to the message.
     *
     * @param target The target message.
     * @return A {@link RestAction RestAction} that will add the emote to the message when executed.
     */
    @Nonnull
    public abstract RestAction<Void> setupEmote(@Nonnull final Message target);

    /**
     * Used to check if an is eligible for this button.
     *
     * @param event The event to check.
     * @return True if the event can be handle by this button, otherwise false.
     */
    protected abstract boolean checkEmote(@Nonnull final GenericMessageReactionEvent event);

    /**
     * Check the event with {@link #checkEmote(GenericMessageReactionEvent)} and triggers the button if the event is
     * valid, otherwise simply return.
     *
     * @param event The event that was triggered on the associated decorator.
     */
    public final void onTrigger(@Nonnull final GenericMessageReactionEvent event) {
        if (checkEmote(event))
            onTrigger.accept(event);
    }

    @Nonnull
    @Override
    public Config serialize() {
        final Config serialized = Config.inMemory();
        serialized.set(Cacheable.KEY_CLASS, getClass().getName());
        if (onTrigger instanceof Serializable)
            serialized.set(Cacheable.KEY_ACTION_FUNCTIONAL,
                    SerializationUtils.serializableToString((Serializable) onTrigger));

        return serialized;
    }
}
