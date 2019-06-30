package com.jesus_crie.modularbot.command.listener;

import com.jesus_crie.modularbot.command.CommandEvent;
import com.jesus_crie.modularbot.command.exception.CommandExecutionException;
import com.jesus_crie.modularbot.command.processing.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;

public class ErrorCommandListener extends CommandAdapter {

    private static final Logger LOG = LoggerFactory.getLogger("CommandListener");

    @Override
    public void onCommandExecutionFailed(@Nonnull final CommandEvent event, @Nonnull final Options options,
                                         @Nonnull final List<String> arguments, @Nonnull final CommandExecutionException error) {
        LOG.error("Command Failed !", error);
    }
}
