package org.slf4j.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Date;

/**
 * Represent a log to handle.
 */
public class ModularLog {

    /**
     * The timestamp (seconds) when the log was emitted.
     */
    public final Date time;

    /**
     * The severity of this log.
     */
    @Nonnull
    public final Level level;

    /**
     * The origin of this log, the name of the logger.
     */
    @Nonnull
    public final String from;

    /**
     * The name of the thread where this log was sent.
     */
    @Nonnull
    public final String thread;

    /**
     * The message, already formatted.
     */
    @Nonnull
    public final String message;

    /**
     * The error, if present.
     */
    @Nullable
    public final Throwable error;

    ModularLog(@Nonnull Date time, @Nonnull Level level, @Nonnull String from, @Nonnull String thread, @Nonnull String message, @Nullable Throwable error) {
        this.time = time;
        this.level = level;
        this.from = from;
        this.thread = thread;
        this.message = message;
        this.error = error;
    }

    public enum Level {
        TRACE("Trace", 0),
        DEBUG("Debug", 1),
        INFO("Info", 2),
        WARN("Warning", 3),
        ERROR("ERROR", 4);

        private final String prefix;
        private final int level;

        Level(String prefix, int level) {
            this.prefix = prefix;
            this.level = level;
        }

        public String getPrefix() {
            return prefix;
        }

        public int getLevel() {
            return level;
        }
    }
}
