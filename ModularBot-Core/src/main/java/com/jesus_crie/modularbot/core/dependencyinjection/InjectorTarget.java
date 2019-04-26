package com.jesus_crie.modularbot.core.dependencyinjection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to mark a constructor suitable for dependency injection.
 * There should be ony one annotated constructor per class or
 * an exception will be thrown during the dependency resolution.
 */
@Target(ElementType.CONSTRUCTOR)
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectorTarget {
}
