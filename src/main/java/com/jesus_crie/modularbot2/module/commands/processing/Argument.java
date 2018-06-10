package com.jesus_crie.modularbot2.module.commands.processing;

import com.jesus_crie.modularbot2.module.commands.CommandModule;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Argument<T> implements Cloneable {

    /**
     * Match any string.
     */
    public static final Argument<String> STRING = new Argument<>(".*", (m, b) -> m.group());

    /**
     * Match a word (string with no whitespace).
     */
    public static final Argument<String> WORD = new Argument<>("[\\S]+", (m, b) -> m.group());

    /**
     * Match a number and convert it to an int, long, or BigInteger depending of its size.
     */
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
     *  - True: 1, t, true, y, yes, on
     *  - False: 0, f, false, n, no, off
     */
    public static final Argument<Boolean> BOOLEAN = new Argument<>("(?:0|1|t(?:rue)?|f(?:alse)?|y(?:es)?|no?|o(?:n|ff))",
            (m, b) -> m.group().matches("(?:1|t(?:rue)?|y(?:es)?|on)"));

    /**
     * Match an URL using the {@link URL#URL(String)} constructor to match an URL and returns it.
     */
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
    public static final Argument<TextChannel> CHANNEL = new Argument<>("<#(?<id>[\\d]+)>", (m, b) -> b.getBot().getTextChannelById(m.group("id")));

    /**
     * Match a mention of a {@link Role Role}.
     */
    public static final Argument<Role> ROLE = new Argument<>("<@&(?<id>[\\d]+)>", (m, b) -> b.getBot().getRoleById(m.group("id")));

    /**
     * Match a custom {@link Emote}, animated or not.
     */
    public static final Argument<Emote> GUILD_EMOTE = new Argument<>("<a?:[\\w]+:(?<id>[0-9]+)>", (m, b) -> b.getBot().getEmoteById(m.group("id")));

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
}
