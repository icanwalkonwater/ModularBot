package com.jesus_crie.modularbot_message_decorator.decorator;

import com.electronwill.nightconfig.core.Config;
import com.jesus_crie.modularbot_message_decorator.Cacheable;
import com.jesus_crie.modularbot_message_decorator.DecoratorListener;
import net.dv8tion.jda.core.entities.Message;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public abstract class MessageDecorator implements Cacheable {

    protected final Message binding;
    protected final long creationTime = System.currentTimeMillis();
    protected final long timeout;
    protected transient List<DecoratorListener> listeners = Collections.emptyList();

    protected boolean isAlive = true;

    /**
     * Build the base of a decorator.
     *
     * @param binding The message to bind to this decorator.
     * @param timeout The amount of time before the decorator expire.
     */
    protected MessageDecorator(@Nonnull final Message binding, final long timeout) {
        this.binding = binding;
        this.timeout = timeout;
    }

    /**
     * Used to check of
     * @param timeout
     * @return
     */
    protected boolean checkTimeout(final long timeout) {
        return timeout >= 0 && isAlive;
    }

    public long getExpireTime() {
        if (timeout == 0 || !isAlive) return 0;
        return creationTime + timeout;
    }

    public boolean isAlive() {
        return isAlive;
    }

    @Override
    public boolean equals(Object obj) {
        return obj.getClass().equals(getClass()) && ((MessageDecorator) obj).binding.equals(binding);
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public Config serialize() {
        final Config serialized = Config.inMemory();
        serialized.set(Cacheable.KEY_CLASS, getClass().getName());
        serialized.set(Cacheable.KEY_BINDING_ID, binding.getIdLong());
        serialized.set(Cacheable.KEY_TIMEOUT, timeout);

        return serialized;
    }
}
