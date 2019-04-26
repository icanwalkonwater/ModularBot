package com.jesus_crie.modularbot.core.dependencyinjection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to mark a class field or a method for late injection.
 * Late injection will be looked after the module instantiation and can only concern
 * already built modules.
 * <p>
 * This can be used to solve circular dependencies.
 * This can also be used to inject optional dependencies <b>but they need to be loaded explicitly somewhere else</b>.
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LateInjectorTarget {
}
