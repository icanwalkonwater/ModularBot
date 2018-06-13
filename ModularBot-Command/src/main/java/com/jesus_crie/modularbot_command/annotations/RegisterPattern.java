package com.jesus_crie.modularbot_command.annotations;

import com.jesus_crie.modularbot_command.Command;
import com.jesus_crie.modularbot_command.CommandEvent;
import com.jesus_crie.modularbot_command.processing.Argument;
import com.jesus_crie.modularbot_command.processing.Options;

import java.lang.annotation.*;

/**
 * Use on a method inside a class that extends {@link Command Command} to associate
 * a pattern with it.
 * The method should be protected or higher and should take 3 arguments in that order:
 *      {@link CommandEvent CommandEvent}, {@link java.util.List List<Object>}, {@link Options Options}.
 * The return type doesn't matter.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RegisterPattern {

    /**
     * The name of the arguments, usually the name of the static field that holds them.
     * For example "STRING" stands for {@link Argument#STRING}.
     */
    String[] arguments() default {};
}
