package com.jesus_crie.modularbot.command.processing;

import com.jesus_crie.modularbot.command.exception.CommandMappingException;
import com.jesus_crie.modularbot.core.utils.TriConsumer;
import com.jesus_crie.modularbot.command.CommandEvent;
import com.jesus_crie.modularbot.command.CommandModule;
import com.jesus_crie.modularbot.command.exception.CommandExecutionException;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandPattern {

    private final List<Argument> arguments;
    private final TriConsumer<CommandEvent, List<Object>, Options> action;

    public CommandPattern(@Nonnull final TriConsumer<CommandEvent, List<Object>, Options> action) {
        arguments = Collections.emptyList();
        this.action = action;
    }

    public CommandPattern(@Nonnull final Argument[] arguments, @Nonnull final TriConsumer<CommandEvent, List<Object>, Options> action) {
        this.arguments = Arrays.asList(arguments);
        this.action = action;
    }

    /**
     * Try to map the given arguments against this pattern and return them mapped to the correct objects.
     *
     * @param module  The module.
     * @param rawArgs The raw arguments.
     * @return A list containing the mapped objects.
     * @throws CommandMappingException If the pattern does not match the arguments.
     */
    @Nonnull
    public List<Object> tryMap(@Nonnull CommandModule module, @Nonnull List<String> rawArgs) throws CommandMappingException {
        if (arguments.size() == 0 && rawArgs.size() > 0
                || rawArgs.size() < arguments.size()
                || (rawArgs.size() > arguments.size() && !getLastArgument().isRepeatable()))
            throw new CommandMappingException("Wrong amount of arguments !");

        final List<Object> args = new ArrayList<>();

        for (int i = 0; i < rawArgs.size(); i++) {
            String raw = rawArgs.get(i);
            Argument argument;
            if (i < arguments.size()) argument = arguments.get(i);
            else argument = getLastArgument();

            Object arg = argument.tryMap(module, raw);
            if (arg == null) throw new CommandMappingException("Failed to map argument [" + raw + "] !");
            args.add(arg);
        }

        return args;
    }

    public void execute(@Nonnull CommandEvent event, @Nonnull Options options, @Nonnull List<Object> arguments)
            throws CommandExecutionException {
        try {
            action.accept(event, arguments, options);
        } catch (RuntimeException e) {
            throw new CommandExecutionException(e.getCause());
        }
    }

    private Argument getLastArgument() {
        return arguments.get(arguments.size() - 1);
    }

    public boolean hasArguments() {
        return !arguments.isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CommandPattern
                && ((CommandPattern) obj).arguments.equals(arguments);
    }

    @Override
    public String toString() {
        return "CommandPattern{ " + arguments + " }";
    }
}
