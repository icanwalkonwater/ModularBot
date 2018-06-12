package com.jesus_crie.modularbot2_command.listener;

import com.jesus_crie.modularbot2_command.Command;
import com.jesus_crie.modularbot2_command.CommandEvent;
import com.jesus_crie.modularbot2_command.CommandModule;
import com.jesus_crie.modularbot2_command.exception.CommandProcessingException;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.utils.tuple.Pair;

import java.util.List;
import java.util.Map;

public class CommandListener extends ListenerAdapter {

    private final CommandModule module;

    public CommandListener(CommandModule module) {
        this.module = module;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().getIdLong() == event.getJDA().getSelfUser().getIdLong())
            return;

        final String prefix = module.getPrefixForGuild(event.getGuild().getIdLong());
        if (!event.getMessage().getContentRaw().startsWith(prefix))
            return;

        final String[] parts = event.getMessage().getContentRaw().split(" ", 2);
        final String name = parts[0].substring(prefix.length());
        final Command command = module.getCommand(name);

        if (command == null) {
            // TODO 10/06/2018 notify not found
            return;
        }

        final CommandEvent cmdEvent = new CommandEvent(event, command);

        if (!command.getAccessLevel().check(cmdEvent)) {
            // TODO 10/06/2018 notify too low access
            return;
        }

        try {
            Pair<List<String>, Map<String, String>> content = module.getCommandProcessor().process(event.getMessage().getContentRaw());
        } catch (CommandProcessingException e) {
            // TODO 11/06/2018 notify error processing command
        }

        // TODO 09/06/2018 stats

        // TODO 09/06/2018 execute cmd
    }
}
