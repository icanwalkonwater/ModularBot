package com.jesus_crie.modularbot2_command.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandInfo {

    /**
     * The aliases of the command.
     * The first will be the name.
     */
    String[] name();

    /**
     * The options to allow for this command.
     * These are the names of the constants registered in {@link com.jesus_crie.modularbot2_command.processing.Options}.
     */
    String[] options() default {};

    /**
     * A short description of the command.
     */
    String shortDescription() default "No description.";

    /**
     * A long description of the command.
     */
    String description() default "No description.";
}
