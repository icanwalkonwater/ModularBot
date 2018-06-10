package com.jesus_crie.modularbot2_command.processing;

import com.jesus_crie.modularbot2_command.CommandModule;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Argument<T> implements Cloneable {

    /**
     * Match any string.
     */
    @RegisterArgument
    public static final Argument<String> STRING = new Argument<>(".*", (m, b) -> m.group());

    /**
     * Match a word (string with no whitespace).
     */
    @RegisterArgument
    public static final Argument<String> WORD = new Argument<>("[\\S]+", (m, b) -> m.group());

    /**
     * Match a number and convert it to an int, long, or BigInteger depending of its size.
     */
    @RegisterArgument
    public static final Argument<Number> INTEGER = new Argument<>("[-+]?[\\d]+", (m, b) -> {
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
    public static final Argument<Number> FLOAT = new Argument<>("([+-]?\\d*\\.?\\d*)", (m, b) -> {
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
    public static final Argument<Boolean> BOOLEAN = new Argument<>("(?:0|1|t(?:rue)?|f(?:alse)?|y(?:es)?|no?|o(?:n|ff))",
            (m, b) -> m.group().matches("(?:1|t(?:rue)?|y(?:es)?|on)"));

    /**
     * Match an URL using the {@link URL#URL(String)} constructor to match an URL and returns it.
     */
    @RegisterArgument
    public static final Argument<URL> URL = new Argument<>(".*", (m, b) -> {
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
    public static final Argument<User> USER = new Argument<>("(?:<@(?<id>[\\d]+)>|(?<name>[\\S]+)#(?<discr>[\\d]{4}))", (m, b) -> {
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
    public static final Argument<TextChannel> CHANNEL = new Argument<>("<#(?<id>[\\d]+)>", (m, b) -> b.getBot().getTextChannelById(m.group("id")));

    /**
     * Match a mention of a {@link Role Role}.
     */
    @RegisterArgument
    public static final Argument<Role> ROLE = new Argument<>("<@&(?<id>[\\d]+)>", (m, b) -> b.getBot().getRoleById(m.group("id")));

    /**
     * Match a custom {@link Emote}, animated or not.
     */
    @RegisterArgument
    public static final Argument<Emote> GUILD_EMOTE = new Argument<>("<a?:[\\w]+:(?<id>[0-9]+)>", (m, b) -> b.getBot().getEmoteById(m.group("id")));

    /**
     * A mapper that maps everything to an empty {@link Object Object}.
     */
    public static BiFunction<Matcher, CommandModule, Object> EMPTY_MAPPER = (m, b) -> new Object();

    /**
     * Create an {@link Argument Argument} that just match a specific string.
     * Automatically escape special characters.
     *
     * @param arg The string to match to.
     * @return An new {@link Argument Argument} already registered.
     */
    public static Argument<Object> forString(@Nonnull String arg) {
        final Argument<Object> a = new Argument<>(
                arg.replaceAll("[-/\\\\^$*+?.()\\[\\]{}]", "\\\\$0"), EMPTY_MAPPER);
        arguments.put(arg, a);
        return a;
    }

    // Auto register
    private static final Map<String, Argument> arguments = new HashMap<>();

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
                    arguments.put(field.getName(), (Argument) field.get(null));
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
        arguments.put(name, argument);
    }

    /**
     * Query a registered argument.
     *
     * @param name The name of the argument (the name of the static field where it come from).
     * @return An {@link Optional} with maybe the argument.
     */
    public static Optional<Argument> getArgument(@Nonnull String name) {
        return Optional.ofNullable(arguments.get(name));
    }

    private final Pattern pattern;
    private final BiFunction<Matcher, CommandModule, T> mapper;
    private boolean repeatable = false;

    public Argument(String regex, BiFunction<Matcher, CommandModule, T> mapper) {
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
        Argument<T> a = clone();
        a.repeatable = true;
        return a;
    }

    public boolean isRepeatable() {
        return repeatable;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Argument<T> clone() {
        try {
            return (Argument<T>) super.clone();
        } catch (CloneNotSupportedException ignore) {
            return null;
        }
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface RegisterArgument {
    }
}
