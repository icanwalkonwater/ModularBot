package com.jesus_crie.modularbot.utils;

import java.io.Serializable;

/**
 * A {@link Runnable Runnable} that can be serialized.
 * Note that if you use variables that are defined outside of this lambda they will be serialized too.
 * A consequence of this is that the usage of this lambda is very limited, you can only use static method without risk,
 * so I recommend using {@link SerializableConsumer SerializableConsumer} so you can pass a variable that can be use without risks.
 *
 * @see Runnable
 * @see SerializableConsumer
 * @see SerializableBiConsumer
 */
@FunctionalInterface
public interface SerializableRunnable extends Runnable, Serializable {
}
