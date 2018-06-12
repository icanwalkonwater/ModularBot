package com.jesus_crie.modularbot2.utils;

public final class Trio<K, V, W> {

    private final K key;
    private final V primary;
    private final W secondary;

    public static <K, V, W> Trio<K, V, W> of(final K k, final V v, final W w) {
        return new Trio<>(k, v, w);
    }

    public Trio(final K key, final V primary, final W secondary) {
        this.key = key;
        this.primary = primary;
        this.secondary = secondary;
    }

    public K getKey() {
        return key;
    }

    public V getPrimary() {
        return primary;
    }

    public W getSecondary() {
        return secondary;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Trio && ((Trio) obj).key == key && ((Trio) obj).primary == primary && ((Trio) obj).secondary == secondary;
    }
}
