package com.jesus_crie.modularbot_command.listener;

import com.jesus_crie.modularbot_command.CommandEvent;
import com.jesus_crie.modularbot_command.exception.CommandProcessingException;
import com.jesus_crie.modularbot_command.exception.UnknownOptionException;
import com.jesus_crie.modularbot_command.processing.Options;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.utils.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

public class NopCommandListener implements CommandListener {

    @Override
    public void onCommandReceived() {
        /* no-op */
    }

    @Override
    public void onCommandFound(@Nonnull CommandEvent command) {
        /* no-op */
    }

    @Override
    public void onCommandNotFound(@Nonnull String name, @Nonnull Message message) {
        /* no-op */
    }

    @Override
    public void onTooLowAccessLevel(@Nonnull CommandEvent event) {
        /* no-op */
    }

    @Override
    public void onCommandSuccessfullyProcessed(@Nonnull CommandEvent event, @Nonnull Pair<List<String>, Map<String, String>> processedContent) {
        /* no-op */
    }

    @Override
    public void onCommandFailedProcessing(@Nonnull CommandEvent event, @Nonnull CommandProcessingException error) {
        /* no-op */
    }

    @Override
    public void onCommandFailedUnknownOption(@Nonnull CommandEvent event, @Nonnull UnknownOptionException error) {
        /* no-op */
    }

    @Override
    public void onCommandFailedNoPatternMatch(@Nonnull CommandEvent event, @Nonnull Options options, @Nonnull List<String> arguments) {
        /* no-op */
    }

    @Override
    public void onCommandSuccess(@Nonnull CommandEvent event) {
        /* no-op */
    }
}
