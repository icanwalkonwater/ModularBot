package com.jesus_crie.modularbot.command;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import javax.annotation.Nonnull;

public class CommandEvent extends MessageReceivedEvent {

    private final CommandModule module;
    private final Command command;

    public CommandEvent(@Nonnull final MessageReceivedEvent baseEvent, @Nonnull final CommandModule module,
                        @Nonnull final Command command) {
        this(baseEvent.getJDA(), baseEvent.getResponseNumber(), baseEvent.getMessage(), module, command);
    }

    public CommandEvent(@Nonnull final JDA api, final long responseNumber, @Nonnull final Message message,
                        @Nonnull final CommandModule module, @Nonnull final Command command) {
        super(api, responseNumber, message);
        this.module = module;
        this.command = command;
    }

    public void fastReply(@Nonnull final String message) {
        channel.sendMessage(message).complete();
    }

    @Nonnull
    public CommandModule getModule() {
        return module;
    }

    @Nonnull
    public Command getCommand() {
        return command;
    }
}
