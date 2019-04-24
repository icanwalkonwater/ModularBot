package com.jesus_crie.modularbot.graalvm.discordjs;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;

import javax.annotation.Nullable;

public abstract class JSPromiseExecutorProxy implements ProxyExecutable {

    private Value resolve;
    private Value reject;

    @Override
    public Object execute(Value... arguments) {
        if (arguments.length != 2) {
            throw new UnsupportedOperationException();
        }

        resolve = arguments[0];
        reject = arguments[1];

        run();
        return null;
    }

    protected void resolve(@Nullable final Object res) {
        resolve.executeVoid(res);
    }

    protected void reject(@Nullable final Object reason) {
        reject.executeVoid(reason);
    }

    public abstract void run();
}
