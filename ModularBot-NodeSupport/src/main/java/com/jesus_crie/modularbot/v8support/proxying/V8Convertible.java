package com.jesus_crie.modularbot.v8support.proxying;

import com.eclipsesource.v8.V8Object;

import javax.annotation.Nonnull;

/**
 * Allow an object to register itself as a proxy object
 * without the need to guess what need to be exported or not.
 */
public interface V8Convertible {

    /**
     * Given an empty {@link V8Object}, the host object as to
     * register the methods that it want to expose to the JS
     * manually.
     *
     * The given object must <b>NOT</b> be released.
     *
     * @param proxy - The proxy object to fill.
     */
    void bindToV8Object(@Nonnull final V8Object proxy);
}
