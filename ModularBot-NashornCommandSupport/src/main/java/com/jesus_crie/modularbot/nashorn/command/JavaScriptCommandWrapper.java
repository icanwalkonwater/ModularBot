package com.jesus_crie.modularbot.nashorn.command;

import com.jesus_crie.modularbot.command.Command;

import javax.annotation.Nonnull;
import java.util.Collections;

/**
 * Class that map the content of a {@link JavaScriptCommand JavaScriptCommand} into a real {@link Command Command}
 * usable by the command module.
 * <p>
 * The command can't be modified after being wrapped.
 */
@Deprecated
public class JavaScriptCommandWrapper extends Command {

    public JavaScriptCommandWrapper(@Nonnull final JavaScriptCommand command) {
        super(command.aliases[0], command.accessLevel, command.shortDescription, command.description);

        aliases.clear();
        Collections.addAll(aliases, command.aliases);
        Collections.addAll(options, command.options);
        Collections.addAll(patterns, command.patterns);
    }
}
