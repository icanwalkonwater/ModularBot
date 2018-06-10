package com.jesus_crie.modularbot2.module.commands.listener;

import com.jesus_crie.modularbot2.module.commands.CommandModule;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class CommandListener extends ListenerAdapter {

    private final CommandModule module;

    public CommandListener(CommandModule module) {
        this.module = module;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().getIdLong() == event.getJDA().getSelfUser().getIdLong())
            return;

        if (!event.getMessage().getContentRaw().startsWith(module.getPrefixForGuild(event.getGuild().getIdLong())))
            return;

        // TODO 09/06/2018 split command & check

        // TODO 09/06/2018 stats

        // TODO 09/06/2018 execute cmd
    }
}
