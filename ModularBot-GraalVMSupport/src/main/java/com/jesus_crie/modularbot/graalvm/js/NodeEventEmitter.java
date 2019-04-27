package com.jesus_crie.modularbot.graalvm.js;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Reflect the behaviour of the EventEmitter of Node.JS as described in the documentation
 * at https://nodejs.org/api/events.html.
 */
public class NodeEventEmitter {

    private static final Logger LOG = LoggerFactory.getLogger("NodeEventEmitter");

    private static final List<Value> EMPTY = Collections.emptyList();
    private final Map<String, List<Value>> listeners = new HashMap<>();

    private int maxListeners = 10;

    public void addListener(@Nonnull final String eventName, @Nonnull final Value listener) {
        on(eventName, listener);
    }

    public void emit(@Nonnull final String eventName, @Nonnull final Object... args) {
        final ListIterator<Value> lsi = listeners.getOrDefault(eventName, EMPTY).listIterator();

        while (lsi.hasNext()) {
            final Value v = lsi.next();

            if (v.isProxyObject() && OnceWrapper.class.isAssignableFrom(v.asProxyObject().getClass())) {
                lsi.remove();
                emit("removeListener", eventName, v);
            }

            v.execute(args);
        }
    }

    @Nonnull
    public List<String> eventNames() {
        return new ArrayList<>(listeners.keySet());
    }

    public int getMaxListeners() {
        return maxListeners;
    }

    public int listenerCount(@Nonnull final String eventName) {
        return listeners.getOrDefault(eventName, EMPTY).size();
    }

    @Nonnull
    public List<Value> listeners(@Nonnull final String eventName) {
        return listeners.getOrDefault(eventName, EMPTY);
    }

    @Nonnull
    public NodeEventEmitter off(@Nonnull final String eventName, @Nonnull final Value listener) {
        removeListener(eventName, listener);
        return this;
    }

    @Nonnull
    public NodeEventEmitter on(@Nonnull final String eventName, @Nonnull final Value listener) {
        emit("newListener", Value.asValue(eventName), listener);

        listeners.computeIfAbsent(eventName, e -> new ArrayList<>());
        if (listeners.get(eventName).size() >= maxListeners)
            LOG.warn("Maximum listeners reached for event: " + eventName);

        listeners.get(eventName).add(listener);

        return this;
    }

    @Nonnull
    public NodeEventEmitter once(@Nonnull final String eventName, @Nonnull final Value listener) {
        on(eventName, new OnceWrapper(listener).wrappedThis);
        return this;
    }

    @Nonnull
    public NodeEventEmitter prependListener(@Nonnull final String eventName, @Nonnull final Value listener) {
        emit("newListener", Value.asValue(eventName), listener);

        listeners.computeIfAbsent(eventName, e -> new ArrayList<>());
        if (listeners.get(eventName).size() >= maxListeners)
            LOG.warn("Maximum listeners reached for event: " + eventName);

        listeners.get(eventName).add(0, listener);

        return this;
    }

    @Nonnull
    public NodeEventEmitter prependOnceEmitter(@Nonnull final String eventName, @Nonnull final Value listener) {
        prependListener(eventName, new OnceWrapper(listener).wrappedThis);
        return this;
    }

    @Nonnull
    public NodeEventEmitter removeAllListeners() {
        listeners.values().forEach(List::clear);
        return this;
    }

    @Nonnull
    public NodeEventEmitter removeAllListeners(@Nonnull final String eventName) {
        final List<Value> ls = listeners.get(eventName);
        if (ls != null) ls.clear();

        return this;
    }

    public void removeListener(@Nonnull final String eventName, @Nonnull final Value listener) {
        final List<Value> ls = listeners.get(eventName);
        if (ls != null) ls.remove(listener);

        emit("removeListener", Value.asValue(eventName), listener);
    }

    public void setMaxListeners(int maxListeners) {
        if (maxListeners < 0)
            throw new IllegalArgumentException("Max listener count can't be negative !");
        else if (maxListeners == Integer.MAX_VALUE)
            maxListeners = 0;

        this.maxListeners = maxListeners;
    }

    @Nonnull
    public List<Value> rawListeners(@Nonnull final String eventName) {
        return listeners.getOrDefault(eventName, EMPTY);
    }

    private class OnceWrapper implements ProxyExecutable {

        private final Value wrappedThis = Value.asValue(this);
        private final Value listener;

        private OnceWrapper(@Nonnull final Value listener) {
            this.listener = listener;
        }

        @Nonnull
        @Override
        public Object execute(Value... arguments) {
            listener.execute((Object[]) arguments);
            return NodeEventEmitter.this;
        }
    }
}
