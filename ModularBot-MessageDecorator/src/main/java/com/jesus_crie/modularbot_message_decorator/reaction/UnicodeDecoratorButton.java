package com.jesus_crie.modularbot_message_decorator.reaction;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.core.requests.RestAction;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class UnicodeDecoratorButton extends DecoratorButton {

    protected final String emote;

    public UnicodeDecoratorButton(@Nonnull final String emote, @Nonnull final Consumer<GenericMessageReactionEvent> onTrigger) {
        super(onTrigger);
        this.emote = emote;
    }

    @Nonnull
    @Override
    public RestAction<Void> setupEmote(@Nonnull Message target) {
        return target.addReaction(emote);
    }

    @Override
    protected boolean checkEmote(@Nonnull GenericMessageReactionEvent event) {
        return !event.getReactionEmote().isEmote() && event.getReactionEmote().getName().equals(emote);
    }
}
