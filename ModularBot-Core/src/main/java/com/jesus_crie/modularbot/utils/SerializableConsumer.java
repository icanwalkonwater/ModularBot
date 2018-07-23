package com.jesus_crie.modularbot.utils;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * A {@link Consumer Consumer} that can be serialized.
 * Note that if you use variables defined outside of this lambda, they will be serialized too.
 * So be sure that the only variable that you use is the parameter of the lambda.
 *
 * @see Consumer
 * @see SerializableBiConsumer
 * @see SerializableRunnable
 */
@FunctionalInterface
public interface SerializableConsumer<T> extends Consumer<T>, Serializable {
}
