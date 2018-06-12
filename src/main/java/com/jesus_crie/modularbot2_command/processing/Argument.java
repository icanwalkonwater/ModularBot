package com.jesus_crie.modularbot2_command.processing;

import com.jesus_crie.modularbot2_command.CommandModule;
import com.jesus_crie.modularbot2_command.annotations.RegisterArgument;
import com.jesus_crie.modularbot2_command.exception.InvalidArgumentException;
import com.jesus_crie.modularbot2_command.exception.InvalidCommandPatternMethodException;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Argument<T> implements Cloneable {

    /**
     * Match any string.
     */
    @RegisterArgument
    public static final Argument<String> STRING = new Argument<>(String.class, ".*", (m, b) -> m.group());

    /**
     * Match a word (string with no whitespace).
     */
    @RegisterArgument
    public static final Argument<String> WORD = new Argument<>(String.class, "[\\S]+", (m, b) -> m.group());

    /**
     * Match a number and convert it to an int, long, or BigInteger depending of its size.
     */
    @RegisterArgument
    public static final Argument<Number> INTEGER = new Argument<>(Number.class, "[-+]?[\\d]+", (m, b) -> {
        String num = m.group();
        try {
            return Integer.parseInt(num);
        } catch (NumberFormatException e) {
            try {
                return Long.parseLong(num);
            } catch (NumberFormatException e2) {
                return new BigInteger(num);
            }
        }
    });

    /**
     * Match a floating point number and convert it to a float or a double.
     */
    @RegisterArgument
    public static final Argument<Number> FLOAT = new Argument<>(Number.class, "([+-]?\\d*\\.?\\d*)", (m, b) -> {
        String num = m.group();
        try {
            return Float.parseFloat(num);
        } catch (NumberFormatException e) {
            try {
                return Double.parseDouble(num);
            } catch (NumberFormatException e1) {
                return 0f;
            }
        }
    });

    /**
     * Match a boolean (case insensitive).
     * - True: 1, t, true, y, yes, on
     * - False: 0, f, false, n, no, off
     */
    @RegisterArgument
    public static final Argument<Boolean> BOOLEAN = new Argument<>(Boolean.class,
            "(?:0|1|t(?:rue)?|f(?:alse)?|y(?:es)?|no?|o(?:n|ff))", (m, b) -> m.group().matches("(?:1|t(?:rue)?|y(?:es)?|on)"));

    /**
     * Match an URL using the {@link URL#URL(String)} constructor to match an URL and returns it.
     */
    @RegisterArgument
    public static final Argument<URL> URL = new Argument<>(java.net.URL.class, ".*", (m, b) -> {
        try {
            return new URL(m.group());
        } catch (MalformedURLException e) {
            return null;
        }
    });

    /**
     * Match a mention of an user or a User#1234.
     */
    @RegisterArgument
    public static final Argument<User> USER = new Argument<>(User.class, "(?:<@(?<id>[\\d]+)>|(?<name>[\\S]+)#(?<discr>[\\d]{4}))", (m, b) -> {
        if (m.group("id") != null) {
            return b.getBot().getUserById(m.group("id"));
        } else {
            String name = m.group("name");
            String discriminator = m.group("discr");
            return b.getBot().getUsers().stream()
                    .filter(u -> u.getDiscriminator().equals(discriminator) && u.getName().equals(name))
                    .findAny().orElse(null);
        }
    });

    /**
     * Match a mention of a {@link TextChannel TextChannel}.
     */
    @RegisterArgument
    public static final Argument<TextChannel> CHANNEL = new Argument<>(TextChannel.class, "<#(?<id>[\\d]+)>", (m, b) -> b.getBot().getTextChannelById(m.group("id")));

    /**
     * Match a mention of a {@link Role Role}.
     */
    @RegisterArgument
    public static final Argument<Role> ROLE = new Argument<>(Role.class, "<@&(?<id>[\\d]+)>", (m, b) -> b.getBot().getRoleById(m.group("id")));

    /**
     * Match a custom {@link Emote}, animated or not.
     */
    @RegisterArgument
    public static final Argument<Emote> GUILD_EMOTE = new Argument<>(Emote.class, "<a?:[\\w]+:(?<id>[0-9]+)>", (m, b) -> b.getBot().getEmoteById(m.group("id")));

    /**
     * A mapper that maps everything to an empty {@link Object Object}.
     */
    public static BiFunction<Matcher, CommandModule, ?> EMPTY_MAPPER = (m, b) -> new Object();

    /**
     * Create an {@link Argument Argument} that just match a specific string.
     * Automatically escape special characters.
     *
     * @param arg The string to match to.
     * @return An new {@link Argument Argument} already registered.
     */
    @SuppressWarnings("unchecked")
    public static Argument forString(@Nonnull String arg) {
        return new Argument(Object.class,
                arg.replaceAll("[-/\\\\^$*+?.()\\[\\]{}]", "\\\\$0"), EMPTY_MAPPER);
    }

    // Auto register
    private static final Map<String, Argument<?>> arguments = new HashMap<>();

    static {
        registerArguments(Argument.class);
    }

    /**
     * Register {@link Argument Argument} constants in a class so they can be retrieve by their name.
     * They must be declared as above, with the public and static modifiers and the
     * {@link RegisterArgument RegisterArgument} annotation.
     *
     * @param clazz The class that contains the fields to register.
     */
    public static void registerArguments(@Nonnull Class<?> clazz) {
        for (Field field : clazz.getFields()) {
            if (Modifier.isStatic(field.getModifiers())
                    && field.isAnnotationPresent(RegisterArgument.class)
                    && field.getType() == Argument.class) {
                try {
                    final Argument<?> arg = (Argument<?>) field.get(null);
                    if (arg.getArgumentsType().equals(Object.class))
                        throw new InvalidArgumentException("Can't register an Argument that map to an Object !");
                    arguments.put(field.getName().toUpperCase(), arg);
                } catch (IllegalAccessException ignore) {
                }
            }
        }
    }

    /**
     * Register an {@link Argument Argument} with the given name.
     *
     * @param name     The name of the argument used to identify it.
     * @param argument The argument to register.
     */
    public static void registerArgument(@Nonnull String name, @Nonnull Argument argument) {
        arguments.put(name.toUpperCase(), argument);
    }

    /**
     * Query a registered argument.
     *
     * @param name The name of the argument (the name of the static field where it come from).
     * @return An possibly-null {@link Argument Argument}.
     */
    @Nullable
    public static Argument<?> getArgument(@Nonnull String name) {
        return arguments.get(name);
    }

    /**
     * Query a registered argument by its type.
     *
     * If there is more that one argument for this type, the closest from the real type is chosen
     * ex: If {@link Integer Integer} is queried and {@link Number Number} and {@link Integer Integer} are available,
     * {@link Integer Integer} will be chosen.
     *
     * If there is equivalent choices the indication is used and the name of the argument is matched (actually it uses a
     * {@link String#startsWith(String)}) to determinate the right choice.
     * THE INDICATION IS USED AS A LAST RESORT.
     *
     * If the ambiguity is still here, an exception is thrown.
     *
     * @param clazz      The target class.
     * @param indication The name of the argument in case of ambiguity.
     * @param <T>        The type of argument needed.
     * @return The {@link Argument Argument} that matches the best for the given inputs.
     * @throws InvalidCommandPatternMethodException If there is too much ambiguity or if there is no match.
     */
    @SuppressWarnings("unchecked")
    @Nonnull
    public static <T> Argument<? super T> getArgument(final @Nonnull Class<T> clazz, final @Nullable String indication) throws InvalidCommandPatternMethodException {

        if (clazz.isAssignableFrom(Collection.class))
            throw new InvalidCommandPatternMethodException("Collections are not allowed as arguments !");

        // Get assignable arguments
        final List<Map.Entry<String, Argument<?>>> assignableMatches = arguments.entrySet().stream()
                .filter(e -> e.getValue().getArgumentsType().isAssignableFrom(clazz))
                .filter(e -> !e.getValue().getArgumentsType().equals(Object.class))
                .collect(Collectors.toList());

        // No matches
        if (assignableMatches.size() == 0)
            throw new InvalidCommandPatternMethodException("No Argument match this type !");
            // One match, don't go further
        else if (assignableMatches.size() == 1)
            return (Argument<? super T>) assignableMatches.get(0).getValue();

        // Rank arguments by depth

        final List<Map.Entry<String, Argument<?>>> closestArguments = new ArrayList<>();
        int minDepth = Integer.MAX_VALUE;

        // Iterate through matches, rank them and add or not
        for (Map.Entry<String, Argument<?>> match : assignableMatches) {
            int depth = computeClassDepth(match.getValue().argumentsType, clazz);
            if (depth < minDepth) {
                minDepth = depth;
                closestArguments.clear();
                closestArguments.add(match);
            } else if (depth == minDepth)
                closestArguments.add(match);
        }

        if (closestArguments.size() == 1) return (Argument<? super T>) closestArguments.get(0);

        // Check the indication

        if (indication == null || indication.equals(""))
            throw new InvalidCommandPatternMethodException("Too much ambiguity and no indication (or maybe you forgot to use \"-parameters\" in the compiler).");

        List<Map.Entry<String, Argument<?>>> matchIndication = closestArguments.stream()
                .filter(e -> e.getKey().startsWith(indication.toUpperCase()))
                .collect(Collectors.toList());

        if (matchIndication.size() == 1)
            return (Argument<? super T>) matchIndication.get(0).getValue();

        if (matchIndication.size() == 0)
            throw new InvalidCommandPatternMethodException("No indication match for this argument !");

        // Try full match indication, who knows
        matchIndication = matchIndication.stream()
                .filter(e -> e.getKey().equalsIgnoreCase(indication))
                .collect(Collectors.toList());

        if (matchIndication.size() == 1)
            return (Argument<? super T>) matchIndication.get(0);

        throw new InvalidCommandPatternMethodException("Too many ambiguity to find a correct Argument," +
                "try to use more precision, like an indication with the exact name of the argument.");
    }

    // Calculate the depth of a class compared to another
    private static <T> int computeClassDepth(@Nonnull Class<?> argument, @Nonnull Class<T> clazz) {
        // If same class, depth 0
        if (argument.equals(clazz)) return 0;

        int depth = 1;
        // For each superclass starting from the direct superclass
        Class<?> superclass = clazz.getSuperclass();
        while (!superclass.equals(argument)) {
            superclass = superclass.getSuperclass();
            depth++;
        }

        return depth;
    }

    private final Class<? extends T> argumentsType;
    private final Pattern pattern;
    private final BiFunction<Matcher, CommandModule, T> mapper;
    private boolean repeatable = false;

    public Argument(@Nonnull Class<? extends T> argumentsType, @Nonnull String regex, @Nonnull BiFunction<Matcher, CommandModule, T> mapper) {
        this.argumentsType = argumentsType;
        this.pattern = Pattern.compile("^" + regex + "$", Pattern.UNICODE_CHARACTER_CLASS | Pattern.CASE_INSENSITIVE);
        this.mapper = mapper;
    }

    @Nullable
    public T tryMap(@Nonnull CommandModule module, @Nonnull String o) {
        final Matcher m = pattern.matcher(o);
        if (!m.find()) return null;
        return mapper.apply(m, module);
    }

    @Nonnull
    public Argument<T> makeRepeatable() {
        final Argument<T> a = clone();
        a.repeatable = true;
        return a;
    }

    public boolean isRepeatable() {
        return repeatable;
    }

    public Class<? extends T> getArgumentsType() {
        return argumentsType;
    }

    @SuppressWarnings({"unchecked", "CloneDoesntDeclareCloneNotSupportedException"})
    @Override
    protected Argument<T> clone() {
        try {
            return (Argument<T>) super.clone();
        } catch (CloneNotSupportedException ignore) {
            return null;
        }
    }

    @Override
    public String toString() {
        return "Argument[" + argumentsType.getSimpleName() + (repeatable ? "..." : "") + "]";
    }
}
