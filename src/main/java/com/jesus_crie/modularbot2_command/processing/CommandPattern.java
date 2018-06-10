package com.jesus_crie.modularbot2_command.processing;

import com.jesus_crie.modularbot2_command.CommandEvent;
import com.jesus_crie.modularbot2_command.CommandModule;
import com.jesus_crie.modularbot2_command.exception.CommandMappingException;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

public class CommandPattern {

    private List<Argument> arguments;
    private BiConsumer<CommandEvent, List<Object>> action;

    public CommandPattern(Argument[] arguments, BiConsumer<CommandEvent, List<Object>> action) {
        this.arguments = Arrays.asList(arguments);
        this.action = action;
    }

    public List<Object> tryMap(@Nonnull CommandModule module, @Nonnull List<String> rawArgs) throws CommandMappingException {
        if (arguments.size() == 0 && rawArgs.size() > 0
                || rawArgs.size() < arguments.size()
                || (rawArgs.size() > arguments.size() && !getLastArgument().isRepeatable()))
            throw new CommandMappingException("Wrong amount of arguments !");

        List<Object> args = new ArrayList<>();

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

    public void execute(@Nonnull CommandEvent event, @Nonnull List<Object> arguments) {
        action.accept(event, arguments);
    }

    private Argument getLastArgument() {
        return arguments.get(arguments.size() - 1);
    }

    public boolean hasArguments() {
        return !arguments.isEmpty();
    }
}
