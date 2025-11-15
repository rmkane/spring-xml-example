package org.example.util;

import java.util.Map;
import java.util.function.Function;

/**
 * Functional interface for formatting map entries.
 * This serves as a type alias for {@code Function<Map.Entry<K, V>, R>}.
 *
 * @param <K> the type of map keys
 * @param <V> the type of map values
 * @param <R> the type of the formatted result
 */
@FunctionalInterface
public interface MapEntryFormatter<K, V, R> extends Function<Map.Entry<K, V>, R> {
}

