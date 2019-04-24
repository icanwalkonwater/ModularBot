package com.jesus_crie.modularbot.command;

import com.jesus_crie.modularbot.command.processing.CommandPattern;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class QuickCommand extends Command {

    public QuickCommand(@Nonnull final String name, @Nonnull final AccessLevel level,
                        @Nonnull final Consumer<CommandEvent> action) {
        super(name, level);
        patterns.add(new CommandPattern(
                (event, options, args) -> action.accept(event)
        ));
    }
}
