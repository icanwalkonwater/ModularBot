package com.jesus_crie.modularbot.v8support;

import com.eclipsesource.v8.Releasable;
import com.eclipsesource.v8.V8Object;
import com.eclipsesource.v8.V8Value;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.hooks.EventListener;

import javax.annotation.Nonnull;

public class V8EventEmitter implements EventListener, Releasable {

    private final V8SupportModule module;
    private V8Object v8EventEmitter;

    public V8EventEmitter(@Nonnull final V8SupportModule module, @Nonnull final V8Object v8EventEmitter) {
        this.module = module;
        this.v8EventEmitter = v8EventEmitter;

        if (v8EventEmitter.getType("emit") != V8Value.V8_FUNCTION) {
            throw new IllegalArgumentException("The given event emitter doesn't have an 'emit' function !");
        }
    }

    @Override
    public void onEvent(final Event event) {
        final V8Object v8Event = module.getOrMakeProxy(event);

        module.acquireLock();

        v8EventEmitter.executeJSFunction("emit", event.getClass().getSimpleName(), v8Event);
        v8Event.close();

        module.releaseLock();
    }

    @Override
    public void close() {
        v8EventEmitter.close();
    }

    @Override
    @Deprecated
    public void release() {
        close();
    }
}
