package com.jesus_crie.modularbot.command.listener;

import com.jesus_crie.modularbot.command.exception.CommandExecutionException;
import com.jesus_crie.modularbot.command.exception.CommandProcessingException;
import com.jesus_crie.modularbot.command.exception.UnknownOptionException;
import com.jesus_crie.modularbot.command.processing.Options;
import com.jesus_crie.modularbot.command.CommandEvent;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

public abstract class CommandAdapter implements CommandListener {

    @Override
    public void onCommandReceived(@Nonnull final MessageReceivedEvent event) {
        /* no-op */
    }

    @Override
    public void onCommandFound(@Nonnull final CommandEvent command) {
        /* no-op */
    }

    @Override
    public void onCommandNotFound(@Nonnull final String name, @Nonnull final Message message) {
        /* no-op */
    }

    @Override
    public void onTooLowAccessLevel(@Nonnull final CommandEvent event) {
        /* no-op */
    }

    @Override
    public void onCommandSuccessfullyProcessed(@Nonnull final CommandEvent event,
                                               @Nonnull final Pair<List<String>,Map<String, String>> processedContent) {
        /* no-op */
    }

    @Override
    public void onCommandFailedProcessing(@Nonnull final CommandEvent event, @Nonnull final CommandProcessingException error) {
        /* no-op */
    }

    @Override
    public void onCommandFailedUnknownOption(@Nonnull final CommandEvent event, @Nonnull final UnknownOptionException error) {
        /* no-op */
    }

    @Override
    public void onCommandFailedNoPatternMatch(@Nonnull final CommandEvent event, @Nonnull final Options options, @Nonnull final List<String> arguments) {
        /* no-op */
    }

    @Override
    public void onCommandExecutionFailed(@Nonnull CommandEvent event, @Nonnull Options options, @Nonnull List<String> arguments, @Nonnull CommandExecutionException error) {
        /* no-op */
    }

    @Override
    public void onCommandSuccess(@Nonnull final CommandEvent event) {
        /* no-op */
    }
}
