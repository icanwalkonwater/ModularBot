package com.jesus_crie.modularbot_message_decorator.reaction;

import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.core.requests.RestAction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

public class EmoteDecoratorButton extends DecoratorButton {

    private final Emote emote;

    public EmoteDecoratorButton(@Nonnull final Emote emote, @Nullable final Consumer<GenericMessageReactionEvent> onTrigger) {
        super(onTrigger);
        this.emote = emote;
    }

    @Nonnull
    @Override
    public RestAction<Void> setupEmote(@Nonnull Message target) {
        return target.addReaction(emote);
    }

    @Override
    protected boolean checkEmote(@Nonnull final GenericMessageReactionEvent event) {
        return event.getReactionEmote().isEmote() && event.getReactionEmote().getEmote().equals(emote);
    }
}
