package com.jesus_crie.modularbot2_command.processing;

import com.jesus_crie.modularbot2_command.annotations.RegisterOption;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Option<T> {

    /**
     * Option that indicate that the user want some help about this pattern.
     * Usage: --help, -h
     */
    @RegisterOption
    public static final Option<Void> HELP = new Option<>("help", 'h');

    /**
     * Option that indicate that no warning should be raised.
     * Usage: --force, -f
     */
    @RegisterOption
    public static final Option<Void> FORCE = new Option<>("force", 'f');

    /**
     * Option that specify a name.
     * Usage: --name, -n
     */
    @RegisterOption
    public static final Option<String> NAME = new Option<>("name", 'n', Argument.STRING);

    /**
     * Option that indicate to do this recursively.
     * Usage: --recursive, -R
     */
    @RegisterOption
    public static final Option<Void> RECURSIVE = new Option<>("recursive", 'R');

    private static final Map<String, Option> options = new HashMap<>();
    static {
        registerOptions(Option.class);
    }

    /**
     * Works exactly the same way as {@link Argument#registerArguments(Class)}.
     *
     * @see Argument
     * @see Argument#registerArguments(Class)
     */
    public static void registerOptions(@Nonnull Class<?> clazz) {
        for (Field field : clazz.getFields()) {
            if (Modifier.isStatic(field.getModifiers())
                    && field.isAnnotationPresent(RegisterOption.class)
                    && field.getType() == Argument.class) {
                try {
                    options.put(field.getName(), (Option) field.get(null));
                } catch (IllegalAccessException ignore) {
                }
            }
        }
    }

    /**
     * Manually register options.
     * The key of the option will be {@link Option#name} but uppercase.
     *
     * @param options The {@link Option Option} to register.
     */
    public static void registerOptions(@Nonnull Option... options) {
        for (Option op : options) {
            Option.options.put(op.name.toUpperCase(), op);
        }
    }

    /**
     * Works exactly the same as {@link Argument#getArgument(String)}.
     *
     * @see Argument
     * @see Argument#getArgument(String)
     */
    public static Optional<Option> getOption(@Nonnull String name) {
        return Optional.ofNullable(options.get(name));
    }

    /**
     * Get an option by its long or short name.
     *
     * @param name The name, short or long.
     * @return An {@link Optional Optional} containing or not the option.
     */
    public static Optional<Option> getOptionByName(@Nonnull String name) {
        if (name.length() == 1) {
            return options.values().stream()
                    .filter(v -> v.getShortName() == name.charAt(0))
                    .findAny();
        } else {
            return options.values().stream()
                    .filter(v -> v.getLongName().equals(name))
                    .findAny();
        }
    }

    private final String name;
    private final char shortName;
    private final Argument<T> argument;

    public Option(@Nonnull String name, char shortName) {
        this(name, shortName, null);
    }

    public Option(@Nonnull String name, char shortName, @Nullable Argument<T> argument) {
        this.name = name;
        this.shortName = shortName;
        this.argument = argument;
    }

    @Nonnull
    public String getLongName() {
        return name;
    }

    public char getShortName() {
        return shortName;
    }

    public boolean hasArgument() {
        return argument == null;
    }

    public Argument<T> getArgument() {
        return argument;
    }
}
