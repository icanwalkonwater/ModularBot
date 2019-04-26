package com.jesus_crie.modularbot.core.utils;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Waiter {

    private static final Logger LOG = LoggerFactory.getLogger("Waiter");

    private static ScheduledExecutorService pool = null;

    public static void init() {
        pool = Executors.newScheduledThreadPool(2, new ModularThreadFactory("Waiter", true));
    }


    // Can't be instantiated.
    private Waiter() {}

    /**
     * Create a listener with the given parameters and return immediately after.
     * <p>
     * This method isn't blocking.
     *
     * @see #createListener(JDA, Class, Predicate, Consumer, Runnable, long, boolean)
     */
    public static <T extends Event> void awaitEvent(@Nonnull final JDA shard, @Nonnull final Class<T> eventClass,
                                                    @Nullable final Predicate<T> checker, @Nullable final Consumer<T> onSuccess,
                                                    @Nullable final Runnable onTimeout, final long timeout, final boolean disposable) {
        createListener(shard, eventClass, checker, onSuccess, onTimeout, timeout, disposable).register();
    }

    /**
     * Create a disposable listener and wait for the event to happen, then return the event or return {@code null} if the
     * listener has timed out or has crashed.
     * <p>
     * This method is blocking.
     *
     * @return The next event of the given type, or {@code null} if timeout.
     * @see #createListener(JDA, Class, Predicate, Consumer, Runnable, long, boolean)
     */
    @Nullable
    public static <T extends Event> T getNextEvent(@Nonnull final JDA shard, @Nonnull final Class<T> eventClass,
                                                   @Nullable final Predicate<T> checker, final long timeout) {
        final WaiterListener<T> listener = createListener(shard, eventClass, checker, null, null, timeout, true);
        listener.register();

        try {
            return listener.get();
        } catch (CancellationException | InterruptedException expected) {
            return null; // The listener has probably timed out.
        } catch (Exception e) {
            LOG.error("A waiter had an unexpected error.", e);
            return null;
        }
    }

    /**
     * Create a disposable listener that will wait for the next message of a {@link User User} in the shard and return
     * the corresponding event.
     * <p>
     * This method is blocking.
     *
     * @return The event corresponding to the next message of the target or {@code null} if timeout.
     */
    @Nullable
    public static MessageReceivedEvent getNextMessageFromUser(@Nonnull final JDA shard, @Nonnull final User target, final long timeout) {
        return getNextEvent(shard, MessageReceivedEvent.class,
                event -> event.getAuthor().equals(target),
                timeout);
    }

    /**
     * Create a disposable listener that will wait for the next message of a {@link User User} in the given {@link MessageChannel MessageChannel}
     * on the current shard.
     * <p>
     * This method is blocking.
     *
     * @return The event corresponding to the message or {@code null} if timeout.
     */
    @Nullable
    public static MessageReceivedEvent getNextMessageFromUserInChannel(@Nonnull final JDA shard, @Nonnull final User target,
                                                                       @Nonnull final MessageChannel targetChannel, final long timeout) {
        return getNextEvent(shard, MessageReceivedEvent.class,
                event -> event.getAuthor().equals(target) && event.getChannel().equals(targetChannel),
                timeout);
    }

    /**
     * Create a listener that will wait for a specific event and can perform various checks and action during this process.
     * It uses the shard's pool to schedule the timeout.
     *
     * @param shard      The shard where the event is supposed to happen.
     * @param eventClass The class of the event to listen to.
     * @param checker    (Optional) A checker to say if the event is valid or not.
     * @param onSuccess  (Optional) Code to execute if the listener successfully catch an event.
     * @param onTimeout  (Optional) Code to execute when the listener will timeout (if a timeout is set).
     * @param timeout    The life time in millisecond of the listener. After that, it will be destroyed. 0 for infinite.
     * @param disposable If true, the listener will trigger at most one time and then complete, otherwise it will continue until it times out.
     * @param <T>        The type of event to listen to.
     * @return A new {@link WaiterListener WaiterListener} configured with the given parameters.
     */
    public static <T extends Event> WaiterListener<T> createListener(@Nonnull final JDA shard, @Nonnull final Class<T> eventClass,
                                                                     @Nullable final Predicate<T> checker, @Nullable final Consumer<T> onSuccess,
                                                                     @Nullable final Runnable onTimeout, final long timeout, final boolean disposable) {
        // Init pool if not done
        if (pool == null) {
            init();
        }

        if (eventClass.getName().equals("com.jesus_crie.modularbot_command.CommandEvent"))
            throw new IllegalArgumentException("You can't wait for CommandEvent, these events aren't triggered.");

        final WaiterListener<T> listener = new WaiterListener<>(shard, eventClass);
        final ScheduledFuture timeoutFuture;

        if (timeout > 0) {
            timeoutFuture = pool.schedule(() -> {
                if (onTimeout != null) onTimeout.run();
                listener.cancel(true);
            }, timeout, TimeUnit.MILLISECONDS);

        } else timeoutFuture = null;

        listener.onTrigger = event -> {
            if (checker == null) return true;

            if (checker.test(event)) {
                if (onSuccess != null) onSuccess.accept(event);

                if (disposable) {
                    if (timeoutFuture != null) timeoutFuture.cancel(true);
                    return true;
                }
            }
            return false;
        };

        return listener;
    }

    public static class WaiterListener<T extends Event> extends CompletableFuture<T> implements EventListener {

        public static final WaiterListener<?> EMPTY = new WaiterListener<>();

        private final JDA shard;
        private final Class<T> eventClass;
        private Predicate<T> onTrigger = null;

        public WaiterListener(@Nonnull final JDA shard, @Nonnull final Class<T> eventClass) {
            this.shard = shard;
            this.eventClass = eventClass;
        }

        private WaiterListener() {
            shard = null;
            eventClass = null;
        }

        public WaiterListener<T> register() {
            if (shard != null)
                shard.addEventListener(this);
            return this;
        }

        public void unregister() {
            if (shard != null)
                shard.removeEventListener(this);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void onEvent(Event event) {
            if (eventClass != null && eventClass.isInstance(event)) {
                if (onTrigger == null)
                    return;

                if (onTrigger.test((T) event)) {
                    unregister();
                    complete((T) event);
                }
            }
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            unregister();
            return super.cancel(mayInterruptIfRunning);
        }
    }
}

