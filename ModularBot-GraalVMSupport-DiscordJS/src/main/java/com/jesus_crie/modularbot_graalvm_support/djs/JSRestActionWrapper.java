package com.jesus_crie.modularbot_graalvm_support.djs;

import net.dv8tion.jda.core.requests.RestAction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;

public class JSRestActionWrapper<T> extends JSPromiseExecutorProxy {

    private final RestAction<?> action;
    private final Function<Object, T> mapper;

    public JSRestActionWrapper(@Nonnull final RestAction<?> action) {
        this(action, null);
    }

    public JSRestActionWrapper(@Nonnull final RestAction<?> action, @Nullable final Function<Object, T> mapper) {
        this.action = action;
        this.mapper = mapper;
    }

    @Override
    public void run() {
        action.queue(res -> {
            if (mapper == null)
                resolve(res);
            else
                resolve(mapper.apply(res));
        }, this::reject);
    }
}
