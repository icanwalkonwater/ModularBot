package com.jesus_crie.modularbot_command.listener;

import com.jesus_crie.modularbot_command.Command;
import com.jesus_crie.modularbot_command.CommandEvent;
import com.jesus_crie.modularbot_command.CommandModule;
import com.jesus_crie.modularbot_command.exception.CommandProcessingException;
import com.jesus_crie.modularbot_command.exception.UnknownOptionException;
import com.jesus_crie.modularbot_command.processing.Options;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.utils.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class DiscordCommandListener extends ListenerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger("DiscordCommandListener");

    private final CommandModule module;

    public DiscordCommandListener(CommandModule module) {
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
            module.getListener().onCommandNotFound(name, event.getMessage());
            return;
        }

        final CommandEvent cmdEvent = new CommandEvent(event, module, command);

        if (!command.getAccessLevel().check(cmdEvent)) {
            module.getListener().onTooLowAccessLevel(cmdEvent);
            return;
        }

        try {
            final String fullArgs;
            if (parts.length == 2) fullArgs = parts[1];
            else fullArgs = "";

            final Pair<List<String>, Map<String, String>> processedContent = module.getCommandProcessor().process(fullArgs);

            module.getListener().onCommandSuccessfullyProcessed(cmdEvent, processedContent);

            final Options options = new Options(module, command, processedContent.getRight());
            if (!command.execute(module, cmdEvent, options, processedContent.getLeft()))
                module.getListener().onCommandFailedNoPatternMatch(cmdEvent, options, processedContent.getLeft());

        } catch (CommandProcessingException e) {
            module.getListener().onCommandFailedProcessing(cmdEvent, e);
        } catch (UnknownOptionException e) {
            module.getListener().onCommandFailedUnknownOption(cmdEvent, e);
        }
    }
}
