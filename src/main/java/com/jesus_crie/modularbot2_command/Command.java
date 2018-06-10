package com.jesus_crie.modularbot2_command;

import com.jesus_crie.modularbot2_command.processing.Argument;
import com.jesus_crie.modularbot2_command.processing.CommandPattern;
import com.jesus_crie.modularbot2_command.processing.Option;
import com.jesus_crie.modularbot2_command.processing.Options;

import javax.annotation.Nonnull;
import java.lang.annotation.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
        for (Method method : getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(RegisterPattern.class)) {

                // Only accept methods with parameters [CommandEvent, List, Options]
                Class<?>[] params = method.getParameterTypes();
                if (params.length == 3
                        && params[0].isAssignableFrom(CommandEvent.class)
                        && params[1].isAssignableFrom(List.class)
                        && params[2].isAssignableFrom(Options.class)) {

                    String[] requestedArgs = method.getAnnotation(RegisterPattern.class).arguments();
                    Argument[] arguments = Arrays.stream(requestedArgs)
                            .map(s -> Argument.getArgument(s).orElseGet(() -> Argument.forString(s)))
                            .toArray(Argument[]::new);
                    patterns.add(new CommandPattern(
                            arguments,
                            (e, a) -> {
                                try {
                                    method.invoke(Command.this, e, a);
                                } catch (IllegalAccessException | InvocationTargetException ignore) {} // Should not occurs
                            }
                    ));
                }
            }
        }
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

    @RegisterPattern(arguments = {"BOOLEAN"})
    public void test(CommandEvent event, List<Object> args, Options options) {

    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    private @interface RegisterPattern {
        String[] arguments() default {};
    }
}
