package com.jesus_crie.modularbot.messagedecorator.decorator;

import com.jesus_crie.modularbot.ModularBot;
import com.jesus_crie.modularbot.utils.Waiter;
import com.jesus_crie.modularbot.messagedecorator.DecoratorListener;
import com.jesus_crie.modularbot.messagedecorator.MessageDecoratorModule;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.Event;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * Class that can extends the behaviour of a discord {@link Message Message} by interacting in specific manners with it.
 * Some of the methods of this class aren't required by every type of decorator so they will throw exceptions.
 */
public abstract class MessageDecorator<T extends Event> {

    protected Message binding;
    protected final long creationTime = System.currentTimeMillis();
    protected final long timeout;
    protected transient Waiter.WaiterListener<T> listener;
    protected transient List<DecoratorListener> listeners = Collections.emptyList();

    protected boolean isAlive = true;

    /**
     * Build the base of a decorator.
     * If the given timeout isn't valid (base on {@link #checkTimeout(long)}) the decorator will call {@link #destroy()}
     * and throw an exception right after.
     *
     * @param binding The message to bind to this decorator.
     * @param timeout The amount of time before the decorator expire.
     * @throws IllegalArgumentException If the given timeout is invalid.
     */
    protected MessageDecorator(@Nonnull final Message binding, final long timeout) {
        if (!checkTimeout(timeout))
            throw new IllegalArgumentException("The given timeout (" + timeout + ") is invalid !");

        this.binding = binding;
        this.timeout = timeout;
    }

    /**
     * Used to create the listener.
     * By default, no listener is created and an exception is thrown instead.
     */
    @Nonnull
    protected Waiter.WaiterListener<T> createListener(@Nonnull final Object... args) {
        throw new AbstractMethodError();
    }

    /**
     * Used to setup the decorator, this must be triggered out of the constructor.
     */
    public void setup() {
    }

    /**
     * Register this decorator.
     * You need to invoke this if you want it to be correctly handled and especially if you want it to be serialized.
     *
     * @param bot The current instance of {@link ModularBot ModularBot}.
     * @see #register(MessageDecoratorModule)
     */
    @SuppressWarnings("ConstantConditions")
    public void register(@Nonnull final ModularBot bot) {
        register(bot.getModuleManager().getModule(MessageDecoratorModule.class));
    }

    /**
     * Register this decorator.
     * You need to invoke this if you want it to be correctly handled and especially if you want it to be serialized.
     *
     * @param module The current {@link MessageDecoratorModule MessageDecoratorModule}.
     * @see #register(ModularBot)
     */
    public void register(@Nonnull final MessageDecoratorModule module) {
        module.registerDecorator(this);
    }

    /**
     * Triggered when the decorator times out.
     * If this method isn't overridden, it will throw an exception but this method isn't called by default.
     * <p>
     * On the other hand, an implementation like {@link ReactionDecorator ReactionDecorator} will call {@link #destroy()}
     * on timeout which is an expected behaviour.
     */
    protected void onTimeout() {
        throw new AbstractMethodError();
    }

    /**
     * Destroy the decorator.
     * <p>
     * The effect of this method might depend of the implementation.
     * Depending of the implementation this method can also be called as a result of {@link #onTimeout()}.
     */
    public abstract void destroy();

    /**
     * Used to check if the given timeout is valid or not.
     *
     * @param timeout The timeout to check.
     * @return True if the timeout is valid, otherwise false.
     */
    protected boolean checkTimeout(final long timeout) {
        return timeout >= 0;
    }

    /**
     * Get the timestamp in milliseconds where the decorator will have expired or 0 for infinite or -1 if the decorator is
     * dead.
     *
     * @return A timestamp in milliseconds or 0.
     */
    public long getExpireTime() {
        if (!isAlive) return 0;
        else if (timeout == 0) return 0;
        return creationTime + timeout;
    }

    /**
     * Get the bound message.
     *
     * @return The bound message.
     */
    public Message getBinding() {
        return binding;
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

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + "[" + binding.getChannel().getIdLong() + " / " + binding.getIdLong() + "]" +
                "[" + (isAlive ? "alive" : "dead") + "]";
    }
}
