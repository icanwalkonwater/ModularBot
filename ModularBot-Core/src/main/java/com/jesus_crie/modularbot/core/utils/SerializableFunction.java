package com.jesus_crie.modularbot.core.utils;

import java.io.Serializable;
import java.util.function.Function;

/**
 * A {@link Function Function} lambda that can be serialized.
 * Note that if you use variables defined outside of this lambda, they will be serialized too.
 * So be sure that the only variable that you use is the parameter of the lambda.
 *
 * @see Function
 * @see SerializableRunnable
 * @see SerializableConsumer
 */
@FunctionalInterface
public interface SerializableFunction<T, R> extends Function<T, R>, Serializable {
}
