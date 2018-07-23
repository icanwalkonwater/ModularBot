package com.jesus_crie.modularbot_command;

import com.jesus_crie.modularbot_command.annotations.CommandInfo;
import com.jesus_crie.modularbot_command.annotations.RegisterPattern;
import com.jesus_crie.modularbot_command.exception.CommandMappingException;
import com.jesus_crie.modularbot_command.exception.InvalidCommandInfoException;
import com.jesus_crie.modularbot_command.exception.InvalidCommandPatternMethodException;
import com.jesus_crie.modularbot_command.processing.Argument;
import com.jesus_crie.modularbot_command.processing.CommandPattern;
import com.jesus_crie.modularbot_command.processing.Option;
import com.jesus_crie.modularbot_command.processing.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Command {

    private static final Logger LOG = LoggerFactory.getLogger("Command");

    protected final List<String> aliases = new ArrayList<>();
    protected final AccessLevel accessLevel;
    protected final String shortDescription;
    protected final String description;

    protected final List<CommandPattern> patterns = new ArrayList<>();
    protected final List<Option> options = new ArrayList<>();

    /**
     * Uses the annotation to fill the fields.
     *
     * @throws InvalidCommandInfoException If the annotation isn't present.
     */
    protected Command() {
        this(AccessLevel.EVERYONE);
    }

    /**
     * Uses the annotation to fill the fields and provide a custom access level.
     *
     * @throws InvalidCommandInfoException If the annotation isn't present
     */
    protected Command(@Nonnull final AccessLevel accessLevel) {
        this.accessLevel = accessLevel;

        if (!getClass().isAnnotationPresent(CommandInfo.class))
            throw new InvalidCommandInfoException("The annotation isn't present on the class !");

        final CommandInfo info = getClass().getAnnotation(CommandInfo.class);
        Collections.addAll(aliases, info.name());
        shortDescription = info.shortDescription();
        description = info.description();

        final String[] rawOptions = info.options();
        for (String raw : rawOptions) {
            Option option = Option.getOption(raw);

            if (option == null)
                throw new InvalidCommandInfoException("Unrecognized option: " + raw);

            else if (options.stream()
                    .anyMatch(o -> o.getShortName() == option.getShortName() || o.getLongName().equals(option.getLongName())))
                throw new InvalidCommandInfoException("An option has the same short or long name than another: " + option);

            options.add(option);
        }

        registerCommandPatterns();
    }

    // Default constructors, does not use the annotation

    protected Command(@Nonnull String name, @Nonnull AccessLevel accessLevel) {
        this(name, accessLevel, "No description");
    }

    protected Command(@Nonnull final String name, @Nonnull final AccessLevel accessLevel,
                      @Nonnull final String shortDescription) {
        this(name, accessLevel, shortDescription, shortDescription);
    }

    protected Command(@Nonnull final String name, @Nonnull final AccessLevel accessLevel,
                      @Nonnull final String shortDescription, @Nonnull final String description) {
        aliases.add(name);
        this.accessLevel = accessLevel;
        this.shortDescription = shortDescription;
        this.description = description;

        registerCommandPatterns();
    }

    /**
     * Convert all registered aliases to lowercase.
     */
    void normalizeAliases() {
        final List<String> cleanAliases = aliases.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());
        aliases.clear();
        aliases.addAll(cleanAliases);
    }

    private void registerCommandPatterns() {
        // For each class starting from the lower to the Object class
        for (Class<?> current = getClass(); current != null; current = current.getSuperclass()) {

            // Iterate all declared method of the class
            for (Method method : current.getDeclaredMethods()) {
                if (method.isAnnotationPresent(RegisterPattern.class)) {

                    // Get type of parameters
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
                                    patterns.add(new CommandPattern((event, args, options) -> invokeMethod(method, event, options)));

                                } else {
                                    // CommandEvent, Arg1
                                    patterns.add(new CommandPattern(
                                            translateArguments(method, 1),
                                            (event, args, options) -> invokeMethod(method, event, args.get(0))
                                    ));
                                }

                                // More than 2 arguments
                            } else {

                                // 3 arguments
                                if (params.length == 3) {

                                    // CommandEvent, Options, ?
                                    if (params[1].isAssignableFrom(Options.class)) {
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

                                        // CommandEvent, Arg1, Arg2
                                    } else {
                                        patterns.add(new CommandPattern(
                                                translateArguments(method, 1),
                                                (event, args, options) -> invokeMethod(method, event, args.get(0), args.get(1))
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
                if (!arguments[pos].getArgumentsType().isAssignableFrom(paramsToTranslate[pos].getType())
                        && (paramsToTranslate[pos].isVarArgs()
                        && !arguments[pos].getArgumentsType().isAssignableFrom(paramsToTranslate[pos].getType().getComponentType())))
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
                if (!parameter.isNamePresent())
                    LOG.warn("Parameters name aren't available ! Maybe you forgot to provide the argument -parameters when compiling ?");

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

    @SuppressWarnings("ConstantConditions")
    private Argument<?>[] translateArguments(@Nonnull final Method method) {
        final RegisterPattern annotation = method.getAnnotation(RegisterPattern.class);
        final Argument<?>[] arguments = new Argument[annotation.arguments().length];

        for (int pos = 0; pos < annotation.arguments().length; pos++) {
            final Argument arg;
            final String toTranslate = annotation.arguments()[pos];

            if (toTranslate.matches("^'[\\S]+'$"))
                arg = Argument.forString(toTranslate.substring(1, toTranslate.length() - 1));
            else if (toTranslate.endsWith("..."))
                arg = Argument.getArgument(toTranslate.substring(0, toTranslate.length() - 3)).makeRepeatable();
            else
                arg = Argument.getArgument(toTranslate);

            if (arg == null)
                throw new InvalidCommandPatternMethodException("Unknown argument at pos " + pos + ": " + Arrays.toString(annotation.arguments()));
            arguments[pos] = arg;
        }

        return arguments;
    }

    private void invokeMethod(@Nonnull Method method, @Nullable Object... args) {
        try {
            method.invoke(this, args);
        } catch (IllegalAccessException | InvocationTargetException ignore) {
        }
    }

    /**
     * Try to execute this command by matching the arguments against the patterns.
     *
     * @param module    The module.
     * @param event     The corresponding event.
     * @param options   The options passed to the command.
     * @param arguments The arguments passed.
     * @return {@code True} if a pattern as successfully matched, otherwise {@code false}.
     */
    public boolean execute(@Nonnull final CommandModule module, @Nonnull final CommandEvent event,
                           @Nonnull final Options options, @Nonnull final List<String> arguments) {
        for (CommandPattern pattern : patterns) {
            try {
                // Try to map the arguments and execute the pattern
                List<Object> args = pattern.tryMap(module, arguments);
                pattern.execute(event, options, args);
                return true;

            } catch (CommandMappingException expected) {
                // If the arguments doesn't match the pattern
            }
        }

        return false;
    }

    @Nonnull
    public String getName() {
        return aliases.get(0);
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
    public String getDescription() {
        return description;
    }

    @Nonnull
    public String getShortDescription() {
        return shortDescription;
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
