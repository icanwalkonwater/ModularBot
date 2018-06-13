package com.jesus_crie.modularbot_command.listener;

import com.jesus_crie.modularbot_command.Command;
import com.jesus_crie.modularbot_command.CommandEvent;
import com.jesus_crie.modularbot_command.CommandModule;
import com.jesus_crie.modularbot_command.exception.CommandProcessingException;
import com.jesus_crie.modularbot_command.processing.Options;
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
            final Pair<List<String>, Map<String, String>> processedContent = module.getCommandProcessor().process(event.getMessage().getContentRaw());

            // TODO 13/06/2018 notify command processed successfully

            final Options options = new Options(module, command, processedContent.getRight());
            if (!command.execute(module, cmdEvent, options, processedContent.getLeft())) {
                // TODO 13/06/2018 notify not pattern found
            }

        } catch (CommandProcessingException e) {
            // TODO 11/06/2018 notify error processing command
        }
    }
}
