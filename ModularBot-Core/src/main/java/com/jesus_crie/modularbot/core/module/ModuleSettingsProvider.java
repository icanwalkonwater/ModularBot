package com.jesus_crie.modularbot.core.module;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

/**
 * Bag of parameters to provide to a module when constructed.
 * The order when building these settings will be the same than in the constructor (after the dependencies).
 */
public final class ModuleSettingsProvider {

    public static ModuleSettingsProvider EMPTY = new ModuleSettingsProvider();

    private int top = 0;
    private final List<Object> settings;

    public ModuleSettingsProvider(@Nonnull final Object... objs) {
        settings = Arrays.asList(objs);
    }

    /**
     * Get the next setting.
     *
     * @param type - The class of the setting, only for convenience.
     * @param <T>  - The type of setting.
     * @return The next setting or null if no settings left.
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T pop(Class<T> type) {
        return (T) pop();
    }

    /**
     * Get the next setting.
     *
     * @return The next setting or null if no settings left.
     */
    @Nullable
    public Object pop() {
        if (top >= settings.size())
            return null;
        return get(top++);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public Object get(final int index) {
        if (index < 0 || index > settings.size())
            return null;
        return settings.get(index);
    }
}
