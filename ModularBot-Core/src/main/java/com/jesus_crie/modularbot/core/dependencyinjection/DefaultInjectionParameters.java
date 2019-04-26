package com.jesus_crie.modularbot.core.dependencyinjection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.jesus_crie.modularbot.core.module.ModuleSettingsProvider;

/**
 * Used to mark a field of type {@link ModuleSettingsProvider} as the default settings to provide
 * to the main injection target.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultInjectionParameters {
}
