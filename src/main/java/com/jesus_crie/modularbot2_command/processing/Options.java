package com.jesus_crie.modularbot2_command.processing;

import com.jesus_crie.modularbot2_command.Command;
import com.jesus_crie.modularbot2_command.CommandModule;
import com.jesus_crie.modularbot2_command.exception.UnknownOptionException;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Options {

    private Map<Option, String> options = new HashMap<>();
    private CommandModule module;

    /**
     * Create a bundle containing utility methods for the {@link Option Option}.
     *
     * @param module  The current {@link CommandModule CommandModule}.
     * @param command The bound {@link Command Command}.
     * @param options The raw options from the processor.
     * @throws UnknownOptionException If one of the options provided is not available for this command.
     */
    public Options(@Nonnull CommandModule module, @Nonnull Command command, @Nonnull Map<String, String> options) throws UnknownOptionException {
        this.module = module;
        final List<Option> availableOptions = command.getOptions();

        options.forEach((op, arg) -> {
            Option option = availableOptions.stream()
                    .filter(o -> o.getShortName() == op.charAt(0) || o.getLongName().equals(op))
                    .findAny()
                    .orElseThrow(() -> new UnknownOptionException(op));
            this.options.put(option, arg);
        });
    }

    /**
     * Check if this option is present.
     *
     * @param option The option.
     * @return True if the option is actually here, otherwise false.
     * @see #has(String)
     * @see #get(Option)
     */
    public boolean has(@Nonnull Option option) {
        return options.containsKey(option);
    }

    /**
     * Check if the option described by the given name (short or long name) is present.
     *
     * @param name The short or long name of the option to look for.
     * @return True if the option is here, otherwise false.
     * @see #has(Option)
     * @see #get(Option)
     */
    public boolean has(@Nonnull String name) {
        if (name.length() == 1) {
            return options.keySet().stream()
                    .anyMatch(k -> k.getShortName() == name.charAt(0));
        } else {
            return options.keySet().stream()
                    .anyMatch(k -> k.getLongName().equals(name));
        }
    }

    /**
     * Lazily map the argument of the queried option.
     *
     * @param option The option to query.
     * @param <T>    The type of the argument, inferred by the option.
     * @return The argument mapped or {@code null} if there is no argument or it failed to map or the option isn't present.
     * @see #has(Option)
     * @see #has(String)
     */
    public <T> T get(@Nonnull Option<T> option) {
        if (!has(option) || !option.hasArgument()) return null;

        String raw = options.get(option);
        if (raw.equals("")) return null;
        return option.getArgument().tryMap(module, raw);
    }
}
