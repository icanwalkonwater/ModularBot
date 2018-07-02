package com.jesus_crie.modularbot.utils;

import java.io.Serializable;

/**
 * A {@link Runnable Runnable} that can be serialized.
 */
@FunctionalInterface
public interface SerializableRunnable extends Runnable, Serializable {
}
