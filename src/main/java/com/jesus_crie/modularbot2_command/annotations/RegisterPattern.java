package com.jesus_crie.modularbot2_command.annotations;

import java.lang.annotation.*;

/**
 * Use on a method inside a class that extends {@link com.jesus_crie.modularbot2_command.Command Command} to associate
 * a pattern with it.
 * The method should be protected or higher and should take 3 arguments in that order:
 *      {@link com.jesus_crie.modularbot2_command.CommandEvent CommandEvent}, {@link java.util.List List<Object>}, {@link com.jesus_crie.modularbot2_command.processing.Options Options}.
 * The return type doesn't matter.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RegisterPattern {

    /**
     * The name of the arguments, usually the name of the static field that holds them.
     * For example "STRING" stands for {@link com.jesus_crie.modularbot2_command.processing.Argument#STRING}.
     */
    String[] arguments() default {};
}
