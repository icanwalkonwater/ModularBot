package com.jesus_crie.modularbot.core.utils;

import java.io.Serializable;
import java.util.function.BiConsumer;

/**
 * A {@link BiConsumer BiConsumer} that can be serialized.
 * Note that if you use variables defined outside of this lambda, they will be serialized too.
 * So be sure that the only variables that you uses are the parameters of the lambda.
 *
 * @see BiConsumer
 * @see SerializableConsumer
 * @see SerializableRunnable
 */
@FunctionalInterface
public interface SerializableBiConsumer<T, U> extends BiConsumer<T, U>, Serializable {
}
