package com.jesus_crie.modularbot2_command;

import com.jesus_crie.modularbot2_command.annotations.RegisterPattern;
import com.jesus_crie.modularbot2_command.exception.InvalidCommandPatternMethodException;
import com.jesus_crie.modularbot2_command.processing.Argument;
import com.jesus_crie.modularbot2_command.processing.CommandPattern;
import com.jesus_crie.modularbot2_command.processing.Option;
import com.jesus_crie.modularbot2_command.processing.Options;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class Command {

    protected final List<String> aliases = new ArrayList<>();
    protected final AccessLevel accessLevel;

    protected final List<CommandPattern> patterns = new ArrayList<>();
    protected final List<Option> options = new ArrayList<>();

    protected Command() {
        // TODO 10/06/2018 annotation
        accessLevel = AccessLevel.EVERYONE; // temp
    }

    protected Command(@Nonnull String name, @Nonnull AccessLevel accessLevel) {
        aliases.add(name);
        this.accessLevel = accessLevel;

        // Register methods pattern

        // For each class starting from the lower to the Object class
        for (Class<?> current = getClass(); current != null; current = current.getSuperclass()) {

            // Iterate all declared method of the class
            for (Method method : current.getDeclaredMethods()) {
                if (method.isAnnotationPresent(RegisterPattern.class)) {

                    // Only accept methods with parameters [CommandEvent, List, Options]
                    Class<?>[] params = method.getParameterTypes();

                    // Only event parameter
                    if (params.length == 0) {
                        // No parameters
                        patterns.add(new CommandPattern((event, args, options) -> invokeMethod(method)));

                    // Have parameter
                    } else {
                        if (!params[0].isAssignableFrom(CommandEvent.class))
                            throw new InvalidCommandPatternMethodException("Invalid method, the first argument must be a CommandEvent: " + method);

                        if (params.length == 1) {
                            // CommandEvent
                            patterns.add(new CommandPattern((event, args, options) -> invokeMethod(method, event)));

                        // More than 1 arg
                        } else {

                            // 2 arguments
                            if (params.length == 2) {
                                if (params[1].isAssignableFrom(List.class)) {
                                    // CommandEvent, List<Object>
                                    patterns.add(new CommandPattern(
                                            translateArguments(method),
                                            (event, args, options) -> invokeMethod(method, event, args)));

                                } else if (params[1].isAssignableFrom(Options.class)) {
                                    // CommandEvent, Options
                                    patterns.add(new CommandPattern((event, args, options) -> invokeMethod(method, options)));

                                } else {
                                    // CommandEvent, Arg1
                                    patterns.add(new CommandPattern(
                                            translateArguments(method, 1),
                                            (event, args, options) -> invokeMethod(method, args.get(0))
                                    ));
                                }

                            // More than 2 arguments
                            } else {
                                if (!params[1].isAssignableFrom(Options.class))
                                    throw new InvalidCommandPatternMethodException("Invalid method, second argument must be a Options: " + method);

                                // 3 arguments
                                if (params.length == 3) {
                                    if (params[2].isAssignableFrom(List.class)) {
                                        // CommandEvent, Options, List<Object>
                                        patterns.add(new CommandPattern(
                                                translateArguments(method),
                                                (event, args, options) -> invokeMethod(method, event, options, args)
                                        ));

                                    } else {
                                        // CommandEvent, Options, Arg1
                                        patterns.add(new CommandPattern(
                                                translateArguments(method, 2),
                                                (event, args, options) -> invokeMethod(method, event, options, args.get(0))
                                        ));
                                    }

                                // More than 3 arguments
                                } else {
                                    // CommandEvent, Options, Args...
                                    patterns.add(new CommandPattern(
                                            translateArguments(method, 2),
                                            (event, args, options) -> invokeMethod(method, event, options, args.toArray(new Object[0]))
                                    ));
                                }
                            }
                        }

                    }
                }
            }
        }
    }

    private Argument<?>[] translateArguments(@Nonnull final Method method, final int startIndex) {
        final RegisterPattern annotation = method.getAnnotation(RegisterPattern.class);
        final Parameter[] paramsToTranslate = Arrays.copyOfRange(method.getParameters(), startIndex, method.getParameterCount());
        final Argument<?>[] arguments;

        // Translate using the annotation
        if (annotation.arguments().length != 0) {
            if (annotation.arguments().length != paramsToTranslate.length)
                throw new InvalidCommandPatternMethodException("Not the same amount of arguments in the annotation and the parameters: " + method);

            arguments = translateArguments(method);

            for (int pos = 0; pos < arguments.length; pos++) {
                if (!arguments[pos].getArgumentsType().isAssignableFrom(paramsToTranslate[pos].getType()))
                    throw new InvalidCommandPatternMethodException("Argument in annotation does not match parameters at position " + pos + ": " + method);

                if (paramsToTranslate[pos].isVarArgs())
                    arguments[pos] = arguments[pos].makeRepeatable();
            }

            return arguments;
        }

        // Translate using parameters type

        arguments = new Argument[paramsToTranslate.length];

        for (int pos = 0; pos < paramsToTranslate.length; pos++) {
            final Parameter parameter = paramsToTranslate[pos];
            try {
                Argument<?> arg = Argument.getArgument(parameter.getType(), parameter.getName());
                if (parameter.isVarArgs())
                    arg = arg.makeRepeatable();

                arguments[pos] = arg;
            } catch (InvalidCommandPatternMethodException e) {
                throw new InvalidCommandPatternMethodException("Malformed argument at method: " + method +
                        "\nParameter: " + parameter +
                        "\nError: " + e.getMessage() +
                        "\nIf you're using complex argument consider providing them using the annotation.");
            }
        }

        return arguments;
    }

    private Argument<?>[] translateArguments(@Nonnull final Method method) {
        final RegisterPattern annotation = method.getAnnotation(RegisterPattern.class);
        final Argument<?>[] arguments = new Argument[annotation.arguments().length];

        for (int pos = 0; pos < annotation.arguments().length; pos++) {
            final Argument arg = Argument.getArgument(annotation.arguments()[pos]);
            if (arg == null)
                throw new InvalidCommandPatternMethodException("Unknown argument at pos " + pos + ": " + Arrays.toString(annotation.arguments()));
            arguments[pos] = arg;
        }

        return arguments;
    }

    private void invokeMethod(@Nonnull Method method, @Nullable Object... args) {
        try {
            method.invoke(this, args);
        } catch (IllegalAccessException | InvocationTargetException ignore) {}
    }

    @Nonnull
    public List<String> getAliases() {
        return aliases;
    }

    @Nonnull
    public AccessLevel getAccessLevel() {
        return accessLevel;
    }

    @Nonnull
    public List<CommandPattern> getPatterns() {
        return Collections.unmodifiableList(patterns);
    }

    @Nonnull
    public List<Option> getOptions() {
        return Collections.unmodifiableList(options);
    }
}
