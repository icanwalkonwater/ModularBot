package com.jesus_crie.modularbot_message_decorator.decorator;

import com.jesus_crie.modularbot.utils.Waiter;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.ShutdownEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;

/**
 * A decorator that will destroy itself and the bound message after a given period of time.
 */
public class AutoDestroyMessageDecorator extends MessageDecorator<ShutdownEvent> {

    /**
     * Create an auto destroy decorator.
     *
     * @param binding   The bound message.
     * @param time      The amount of time in the given unit before the decorator and the message will be destroyed.
     * @param unit      The unit of time.
     * @param onTimeout (Optional) An additional action to perform before deleting the message.
     */
    public AutoDestroyMessageDecorator(@Nonnull final Message binding, final long time, final TimeUnit unit,
                                       @Nullable final Runnable onTimeout) {
        super(binding, unit.toMillis(time));
        listener = createListener(onTimeout);
    }

    @Nonnull
    @Override
    protected Waiter.WaiterListener<ShutdownEvent> createListener(@Nonnull Object... args) {
        return Waiter.createListener(binding.getJDA(), ShutdownEvent.class, null, null,
                () -> {
                    if (args.length > 0) ((Runnable) args[0]).run();
                    onTimeout();
                },
                timeout, true);
    }

    /**
     * Setup useless here.
     * The only thing that matters is the timeout which is set when the listener is created.
     */
    @Override
    public void setup() {
        /* no-op */
    }

    @Override
    protected void onTimeout() {
        destroy();
    }

    @Override
    public void destroy() {
        binding.delete().complete();
    }
}
