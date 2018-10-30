package com.jesus_crie.modularbot_command.listener;

import com.jesus_crie.modularbot_command.Command;
import com.jesus_crie.modularbot_command.CommandEvent;
import com.jesus_crie.modularbot_command.CommandModule;
import com.jesus_crie.modularbot_command.exception.CommandExecutionException;
import com.jesus_crie.modularbot_command.exception.CommandProcessingException;
import com.jesus_crie.modularbot_command.exception.UnknownOptionException;
import com.jesus_crie.modularbot_command.processing.Options;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.utils.tuple.Pair;

import java.util.List;
import java.util.Map;

public class DiscordCommandListener extends ListenerAdapter {

    private final CommandModule module;

    public DiscordCommandListener(CommandModule module) {
        this.module = module;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().getIdLong() == event.getJDA().getSelfUser().getIdLong())
            return;

        final String prefix = module.getPrefixForGuild(event.getGuild());
        if (!event.getMessage().getContentRaw().startsWith(prefix))
            return;

        module.triggerListeners(l -> l.onCommandReceived(event));

        final String[] parts = event.getMessage().getContentRaw().split(" ", 2);
        final String name = parts[0].substring(prefix.length());
        final Command command = module.getCommand(name);

        if (command == null) {
            // Command not found
            module.triggerListeners(l -> l.onCommandNotFound(name, event.getMessage()));
            return;
        }

        final CommandEvent cmdEvent = new CommandEvent(event, module, command);

        module.triggerListeners(l -> l.onCommandFound(cmdEvent));

        if (!command.getAccessLevel().check(cmdEvent)) {
            // Too low access level
            module.triggerListeners(l -> l.onTooLowAccessLevel(cmdEvent));
            return;
        }

        try {
            final String fullArgs;
            if (parts.length == 2) fullArgs = parts[1];
            else fullArgs = "";

            final Pair<List<String>, Map<String, String>> processedContent = module.getCommandProcessor().process(fullArgs);

            // Successfully processed
            module.triggerListeners(l -> l.onCommandSuccessfullyProcessed(cmdEvent, processedContent));

            final Options options = new Options(module, command, processedContent.getRight());

            try {
                if (!command.execute(module, cmdEvent, options, processedContent.getLeft()))
                    // No pattern match
                    module.triggerListeners(l -> l.onCommandFailedNoPatternMatch(cmdEvent, options, processedContent.getLeft()));
                else module.triggerListeners(l -> l.onCommandSuccess(cmdEvent));
            } catch (CommandExecutionException e) {
                // Command failed
                module.triggerListeners(l -> l.onCommandExecutionFailed(cmdEvent, options, processedContent.getLeft(), e));
            }

        } catch (CommandProcessingException e) {
            // Fail processing
            module.triggerListeners(l -> l.onCommandFailedProcessing(cmdEvent, e));
        } catch (UnknownOptionException e) {
            // Unknown option
            module.triggerListeners(l -> l.onCommandFailedUnknownOption(cmdEvent, e));
        }
    }
}
