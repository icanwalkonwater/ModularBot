package com.jesus_crie.modularbot.core.dependencyinjection;

import com.jesus_crie.modularbot.core.module.Module;
import net.dv8tion.jda.core.utils.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Registry of the relations between each module.
 * Useful for hot swapping modules.
 */
public class DependencyGraph {

    private static final Pair<List<Class<? extends Module>>, List<Class<? extends Module>>> EMPTY =
            Pair.of(Collections.emptyList(), Collections.emptyList());

    /**
     * Map meaning:
     * - Module
     * -> Left: parents (dependencies of the module)
     * -> Right: children (modules that depend on the module)
     */
    private final Map<
            Class<? extends Module>, Pair<List<Class<? extends Module>>, List<Class<? extends Module>>>> dependencyGraph = new HashMap<>();

    /**
     * Register a parent (dependency) of the current module.
     *
     * @param actual - The current module.
     * @param parent - The parent (dependency) of the current module.
     */
    public void registerParent(@Nonnull final Class<? extends Module> actual, @Nonnull final Class<? extends Module> parent) {
        ensureEntryExist(actual);
        dependencyGraph.get(actual).getLeft().add(parent);
    }

    /**
     * Register a child (that depends on) of the current module.
     *
     * @param actual - The current module.
     * @param child  - The child (dependency) module of the current module.
     */
    public void registerChild(@Nonnull final Class<? extends Module> actual, @Nonnull final Class<? extends Module> child) {
        ensureEntryExist(actual);
        dependencyGraph.get(actual).getRight().add(child);
    }

    /**
     * Get view component of the graph, from where you can query the graph but not alter it.
     *
     * @return A view of the graph.
     */
    public View asView() {
        return new View();
    }

    private void ensureEntryExist(@Nonnull final Class<? extends Module> actual) {
        dependencyGraph.putIfAbsent(
                actual,
                Pair.of(new ArrayList<>(), new ArrayList<>())
        );
    }

    public class View {

        /**
         * Get the parents (dependencies) of the current module.
         *
         * @param actual - The current module.
         * @return A possibly-empty list of the parents of the current module.
         */
        @Nonnull
        public List<Class<? extends Module>> getParents(@Nonnull final Class<? extends Module> actual) {
            return dependencyGraph.getOrDefault(actual, EMPTY).getLeft();
        }

        /**
         * Recursively collect all of the ancestors of the current module.
         *
         * @param actual - The current module.
         * @return A possibly-empty list of every ancestor of the current module.
         */
        public List<Class<? extends Module>> getAncestors(@Nonnull final Class<? extends Module> actual) {
            return getParents(actual).stream()
                    .flatMap(parent -> getAncestors(parent).stream())
                    .collect(Collectors.toList());
        }

        /**
         * Get the children (that depends on) of the current module.
         *
         * @param actual - The current module.
         * @return A possibly-empty list of the children of the current module.
         */
        @Nonnull
        public List<Class<? extends Module>> getChildren(@Nonnull final Class<? extends Module> actual) {
            return dependencyGraph.getOrDefault(actual, EMPTY).getRight();
        }

        /**
         * Recursively collect all of the descendants of the current module.
         *
         * @param actual - The current module.
         * @return All possibly-empty list of the descendants of the current module.
         */
        public List<Class<? extends Module>> getDescendants(@Nonnull final Class<? extends Module> actual) {
            return getChildren(actual).stream()
                    .flatMap(child -> getDescendants(child).stream())
                    .collect(Collectors.toList());
        }
    }
}
