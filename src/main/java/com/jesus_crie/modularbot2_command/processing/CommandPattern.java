package com.jesus_crie.modularbot2_command.processing;

import com.jesus_crie.modularbot2.utils.TriConsumer;
import com.jesus_crie.modularbot2_command.CommandEvent;
import com.jesus_crie.modularbot2_command.CommandModule;
import com.jesus_crie.modularbot2_command.exception.CommandMappingException;

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

    public void execute(@Nonnull CommandEvent event, @Nonnull List<Object> arguments, @Nonnull Options options) {
        action.accept(event, arguments, options);
    }

    private Argument getLastArgument() {
        return arguments.get(arguments.size() - 1);
    }

    public boolean hasArguments() {
        return !arguments.isEmpty();
    }
}
