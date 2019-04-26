package com.jesus_crie.modularbot.core.dependencyinjection.exception;

import com.jesus_crie.modularbot.core.module.Module;

import javax.annotation.Nonnull;
import java.util.Queue;
import java.util.stream.Collectors;

public class CircularDependencyException extends DependencyInjectionException {

    public CircularDependencyException(@Nonnull final Queue<Class<? extends Module>> hierarchy,
                                       @Nonnull final Class<? extends Module> module) {
        super(String.format("Circular dependency detected: %s -> {%s}",
                hierarchy.stream()
                        .map(dep -> {
                            if (dep.equals(module))
                                return "{" + dep.getSimpleName() + "}";
                            return dep.getSimpleName();
                        })
                        .collect(Collectors.joining(" -> ")),
                module.getSimpleName()
        ));
    }
}
