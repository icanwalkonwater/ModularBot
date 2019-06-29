package com.jesus_crie.modularbot.v8support.proxying;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class to create rules that allow the proxy maker to
 * be more configurable regarding certain special cases in case
 * the targeted object can't be annotated with {@link ProxyExport}.
 */
public class ProxyRules {

    private final boolean onlyDominant;
    private final List<Method> dominantMethods;

    public static ProxyRules dominatedBy(@Nonnull final Method... dominantMethods) {
        return new ProxyRules(false, Arrays.asList(dominantMethods));
    }

    public static ProxyRules onlyKeep(@Nonnull final Method... dominantMethods) {
        return new ProxyRules(true, Arrays.asList(dominantMethods));
    }

    private ProxyRules(final boolean onlyDominant, @Nonnull final List<Method> dominantMethods) {
        this.onlyDominant = onlyDominant;
        this.dominantMethods = dominantMethods;
    }

    /**
     * @return True if only the dominant methods should be kept, otherwise false.
     */
    public boolean isOnlyDominant() {
        return onlyDominant;
    }

    /**
     * @return The dominant methods.
     */
    public List<Method> getDominantMethods() {
        return dominantMethods;
    }

    /**
     * Check whether the given method is dominant or not.
     *
     * @param method - The method to check.
     * @return True if the method is dominant, otherwise false.
     */
    public boolean isDominant(@Nonnull final Method method) {
        return dominantMethods.contains(method);
    }
}
