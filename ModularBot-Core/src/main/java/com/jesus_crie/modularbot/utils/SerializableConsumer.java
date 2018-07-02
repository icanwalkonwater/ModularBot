package com.jesus_crie.modularbot.utils;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * A {@link Consumer Consumer} that can be serialized.
 */
public interface SerializableConsumer<T> extends Consumer<T>, Serializable {
}
