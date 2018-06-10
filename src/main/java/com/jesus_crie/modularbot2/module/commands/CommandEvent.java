package com.jesus_crie.modularbot2.module.commands;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandEvent extends MessageReceivedEvent {

    private Command command;

    public CommandEvent(JDA api, long responseNumber, Message message, Command command) {
        super(api, responseNumber, message);
        this.command = command;
    }

    public Command getCommand() {
        return command;
    }
}
