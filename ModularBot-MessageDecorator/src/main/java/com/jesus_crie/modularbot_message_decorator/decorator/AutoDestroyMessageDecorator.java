package com.jesus_crie.modularbot_message_decorator.decorator;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.Event;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

public class AutoDestroyMessageDecorator extends MessageDecorator<Event> {

    public AutoDestroyMessageDecorator(@Nonnull final Message binding, final long time, final TimeUnit unit) {
        super(binding, unit.toMillis(time));
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
