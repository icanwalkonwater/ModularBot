package com.jesus_crie.modularbot_message_decorator.button;

import com.jesus_crie.modularbot.utils.SerializableConsumer;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.core.requests.RestAction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UnicodeDecoratorButton extends DecoratorButton {

    protected final String emote;

    public UnicodeDecoratorButton(@Nonnull final String emote, @Nullable final SerializableConsumer<GenericMessageReactionEvent> onTrigger) {
        super(onTrigger);
        this.emote = emote;
    }

    @Nonnull
    @Override
    public RestAction<Void> setupEmote(@Nonnull Message target) {
        return target.addReaction(emote);
    }

    @Override
    public RestAction<Void> removeEmote(@Nonnull Message target) {
        return target.getReactions().stream()
                .filter(mr -> mr.getReactionEmote().getName().equals(emote))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("The reaction is missing on the bound message !"))
                .removeReaction();
    }

    @Override
    protected boolean checkEmote(@Nonnull GenericMessageReactionEvent event) {
        return !event.getReactionEmote().isEmote() && event.getReactionEmote().getName().equals(emote);
    }

    @Nonnull
    @Override
    public String getEmoteSerialized() {
        return emote;
    }
}
