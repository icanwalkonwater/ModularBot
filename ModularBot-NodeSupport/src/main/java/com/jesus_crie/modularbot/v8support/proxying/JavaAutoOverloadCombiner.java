package com.jesus_crie.modularbot.v8support.proxying;

import com.eclipsesource.v8.JavaCallback;
import com.eclipsesource.v8.Releasable;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * A basic wrapper around several java methods with the same name (overloads).
 * <p>
 * When invoked, will execute the first overload that matches the arguments provided.
 */
public class JavaAutoOverloadCombiner implements JavaCallback {

    private final Object targetInstance;
    private final Map<Integer, List<Method>> methodsByParameterCount = new HashMap<>();

    public JavaAutoOverloadCombiner(@Nonnull final Object target, @Nonnull final List<Method> methods) {
        targetInstance = target;

        for (Method method : methods) {
            // Don't manage methods with varargs
            if (!method.isVarArgs()) {
                methodsByParameterCount.computeIfAbsent(method.getParameterCount(), count -> new ArrayList<>())
                        .add(method);
            }
        }
    }

    @Override
    public Object invoke(@Nonnull final V8Object receiver, @Nonnull final V8Array parameters) {
        final Method overload = findMethod(parameters);

        final Object[] javaParameters = new Object[parameters.length()];
        for (int i = 0; i < javaParameters.length; ++i) {
            javaParameters[i] = parameters.get(i);
        }

        try {
            return overload.invoke(targetInstance, javaParameters);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        } finally {
            releaseParameters(parameters);
        }
    }

    /**
     * Find the first overload that can be executed with the given parameters.
     * <p>
     * The algorithm will first filter by parameter count (varargs aren't supported)
     * and will then find the first overload which matches the types of the parameters.
     *
     * @param parameters - The v8 parameters.
     * @return A suitable overload for the given parameters.
     * @throws IllegalStateException If no method matches the parameters.
     */
    private Method findMethod(@Nonnull final V8Array parameters) {
        try {

            // *** Try to determine overload by param count ***

            // No overload with the same amount of parameters
            // check varargs
            if (!methodsByParameterCount.containsKey(parameters.length())) {
                throw new NoSuchMethodException("No methods with the same parameter length");
            }

            // Create candidates
            final List<Method> candidates = new ArrayList<>(methodsByParameterCount.get(parameters.length()));

            // Only one overload, go for it
            if (candidates.size() == 1) {
                return candidates.get(0);
            }

            // *** Take first correct signature ***

            // Iterate through candidates
            candidateLoop:
            for (final Iterator<Method> iterator = candidates.iterator(); iterator.hasNext(); ) {
                final Method candidate = iterator.next();
                final Class<?>[] parameterTypes = candidate.getParameterTypes();

                // Iterate through each parameters
                for (int i = 0; i < parameterTypes.length; ++i) {
                    final Class<?> type = parameterTypes[i];

                    // Detect signature mismatch
                    if (!type.isAssignableFrom(parameters.get(i).getClass())) {
                        iterator.remove();
                        continue candidateLoop;
                    }
                }

                // If we reach this, the signature is a match
                // Stop here
                return candidate;
            }

            // If we reach this, no method has the correct signature

            // No more candidates
            if (candidates.isEmpty())
                throw new NoSuchMethodException("No overload with same signature");

            // Otherwise there is at least one more candidate, take the first one
            return candidates.get(0);

        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Automatically release the content of the given array.
     *
     * @param parameters - The array to release.
     */
    private void releaseParameters(final V8Array parameters) {
        final int length = parameters.length();
        for (int i = 0; i < length; ++i) {

            final Object o = parameters.get(i);
            if (o instanceof Releasable) {
                ((Releasable) o).close();
            }
        }
    }
}
