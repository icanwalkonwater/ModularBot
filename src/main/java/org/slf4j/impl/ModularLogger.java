package org.slf4j.impl;

import org.slf4j.helpers.MarkerIgnoringBase;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.time.Instant;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.jesus_crie.modularbot2.utils.F.f;

/**
 * Implementation of SLF4J {@link org.slf4j.Logger} for ModularBot.
 * Allow you to listen to the logs that pass through here.
 */
public class ModularLogger extends MarkerIgnoringBase {

    private static final CopyOnWriteArrayList<OnLogListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * Add a listener that will receive all of the logs that pass through here.
     *
     * @param listener The listener to register.
     */
    public static void addListener(@Nonnull OnLogListener listener) {
        listeners.add(listener);
    }

    /**
     * Unregister a listener.
     *
     * @param listener The listener to remove.
     */
    public static void removeListener(@Nonnull OnLogListener listener) {
        listeners.remove(listener);
    }

    private final String name;

    ModularLogger(String name) {
        this.name = name;
    }

    private void log(@Nonnull ModularLog.Level level, @Nullable Throwable error, @Nonnull String message) {
        final ModularLog log = new ModularLog(new Date(),
                level, name, Thread.currentThread().getName(), message, error);
        listeners.forEach(l -> l.onLog(log));
    }

    @Override
    public boolean isTraceEnabled() {
        return true;
    }

    @Override
    public void trace(@Nonnull String msg) {
        log(ModularLog.Level.TRACE, null, msg);
    }

    @Override
    public void trace(@Nonnull String msg, @Nonnull Throwable t) {
        log(ModularLog.Level.TRACE, t, msg);
    }

    @Override
    public void trace(@Nonnull String format, @Nonnull Object arg) {
        trace(f(format, arg));
    }

    @Override
    public void trace(@Nonnull String format, @Nonnull Object arg1, @Nonnull Object arg2) {
        trace(f(format, arg1, arg2));
    }

    @Override
    public void trace(@Nonnull String format, @Nonnull Object... arguments) {
        trace(f(format, arguments));
    }

    @Override
    public boolean isDebugEnabled() {
        return true;
    }

    @Override
    public void debug(@Nonnull String msg) {
        log(ModularLog.Level.DEBUG, null, msg);
    }

    @Override
    public void debug(@Nonnull String msg, @Nonnull Throwable t) {
        log(ModularLog.Level.DEBUG, t, msg);
    }

    @Override
    public void debug(@Nonnull String format, @Nonnull Object arg) {
        debug(f(format, arg));
    }

    @Override
    public void debug(@Nonnull String format, @Nonnull Object arg1, @Nonnull Object arg2) {
        debug(f(format, arg1, arg2));
    }

    @Override
    public void debug(@Nonnull String format, @Nonnull Object... arguments) {
        debug(f(format, arguments));
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public void info(@Nonnull String msg) {
        log(ModularLog.Level.INFO, null, msg);
    }

    @Override
    public void info(@Nonnull String msg, @Nonnull Throwable t) {
        log(ModularLog.Level.INFO, t, msg);
    }

    @Override
    public void info(@Nonnull String format, @Nonnull Object arg) {
        info(f(format, arg));
    }

    @Override
    public void info(@Nonnull String format, @Nonnull Object arg1, @Nonnull Object arg2) {
        info(f(format, arg1, arg2));
    }

    @Override
    public void info(@Nonnull String format, @Nonnull Object... arguments) {
        info(f(format, arguments));
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public void warn(@Nonnull String msg) {
        log(ModularLog.Level.WARN, null, msg);
    }

    @Override
    public void warn(@Nonnull String msg, @Nonnull Throwable t) {
        log(ModularLog.Level.WARN, t, msg);
    }

    @Override
    public void warn(@Nonnull String format, @Nonnull Object arg) {
        warn(f(format, arg));
    }

    @Override
    public void warn(@Nonnull String format, @Nonnull Object arg1, @Nonnull Object arg2) {
        warn(f(format, arg1, arg2));
    }

    @Override
    public void warn(@Nonnull String format, @Nonnull Object... arguments) {
        warn(f(format, arguments));
    }


    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public void error(@Nonnull String msg) {
        log(ModularLog.Level.ERROR, null, msg);
    }

    @Override
    public void error(@Nonnull String msg, @Nonnull Throwable t) {
        log(ModularLog.Level.ERROR, t, msg);
    }

    @Override
    public void error(@Nonnull String format, @Nonnull Object arg) {
        error(f(format, arg));
    }

    @Override
    public void error(@Nonnull String format, @Nonnull Object arg1, @Nonnull Object arg2) {
        error(f(format, arg1, arg2));
    }

    @Override
    public void error(@Nonnull String format, @Nonnull Object... arguments) {
        error(f(format, arguments));
    }

    @FunctionalInterface
    public interface OnLogListener {

        void onLog(@Nonnull ModularLog log);
    }
}
