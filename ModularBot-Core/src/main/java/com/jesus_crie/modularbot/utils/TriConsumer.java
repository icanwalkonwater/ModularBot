package com.jesus_crie.modularbot.utils;

import java.util.Objects;

/**
 * Same as {@link java.util.function.BiConsumer BiConsumer} and {@link java.util.function.Consumer Consumer} but with
 * 3 arguments.
 *
 * @see java.util.function.Consumer
 * @see java.util.function.BiConsumer
 */
@FunctionalInterface
public interface TriConsumer<T, V, K> {

    void accept(T t, V v, K k);

    default TriConsumer<T, V, K> andThen(TriConsumer<? super T, ? super V, ? super K> after) {
        Objects.requireNonNull(after);

        return (t, v, k) -> {
            accept(t, v, k);
            after.accept(t, v, k);
        };
    }
}
