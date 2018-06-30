package com.jesus_crie.modularbot_message_decorator.decorator;

import com.electronwill.nightconfig.core.Config;
import com.jesus_crie.modularbot.utils.Waiter;
import com.jesus_crie.modularbot_message_decorator.Cacheable;
import com.jesus_crie.modularbot_message_decorator.DecoratorListener;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.Event;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * Class that can extends the behaviour of a discord {@link Message Message} by interacting in specific manners with it.
 */
public abstract class MessageDecorator<T extends Event> implements Cacheable {

    protected Message binding;
    protected final long creationTime = System.currentTimeMillis();
    protected final long timeout;
    protected transient Waiter.WaiterListener<T> listener;
    protected transient List<DecoratorListener> listeners = Collections.emptyList();

    protected boolean isAlive = true;

    /**
     * Build the base of a decorator.
     *  @param binding The message to bind to this decorator.
     * @param timeout The amount of time before the decorator expire.
     */
    protected MessageDecorator(@Nonnull final Message binding, final long timeout) {
        if (!checkTimeout(timeout))
            throw new IllegalArgumentException("The given timeout (" + timeout + ") is invalid !");

        this.binding = binding;
        this.timeout = timeout;
    }

    /**
     * Used to create the listener.
     */
    protected Waiter.WaiterListener<T> createListener(@Nonnull final Object... args) {
        throw new AbstractMethodError();
    }

    /**
     * Used to setup the decorator, this must be triggered out of the constructor.
     */
    public void setup() {}

    /**
     * Triggered when the decorator times out.
     * Some implementation might alter the way that this method is called but most of them will call {@link #destroy()}.
     */
    protected void onTimeout() {
        throw new AbstractMethodError();
    }

    /**
     * Destroy the decorator.
     *
     * The effect of this method might depend of the implementation.
     * Depending of the implementation this method can also be called as a result of {@link #onTimeout()}.
     */
    public abstract void destroy();

    /**
     * Used to check if the given timeout is valid or not.
     *
     * @param timeout The timeout to check.
     * @return
     */
    protected boolean checkTimeout(final long timeout) {
        return timeout >= 0;
    }

    /**
     * Get the timestamp in milliseconds where the decorator will have expired or 0 for infinite or if the decorator is
     * dead.
     *
     * @return A timestamp in milliseconds or 0.
     */
    public long getExpireTime() {
        if (timeout == 0 || !isAlive) return 0;
        return creationTime + timeout;
    }

    /**
     * Update the reference to the bound message by querying it again from JDA.
     */
    protected void updateMessage() {
        binding = binding.getChannel().getMessageById(binding.getIdLong()).complete();
    }

    /**
     * Check if the decorator is still alive.
     *
     * @return True if its alive, otherwise false.
     */
    public boolean isAlive() {
        return isAlive;
    }

    /**
     * Check if 2 decorators are the same. They must be an instance of the same decorator and be bound to the same binding.
     *
     * @param obj The other potentially equal decorator.
     * @return True if they are equivalent.
     */
    @Override
    public boolean equals(Object obj) {
        return obj.getClass() == getClass() && ((MessageDecorator) obj).binding.equals(binding);
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
