package activity.misc;

import java.io.Serializable;
import java.util.Objects;

// Pair: A generic class that represents a key-value pair
public class Pair<K, V> implements Serializable
{
    private final K key;
    private final V value;

    public Pair(K key, V value)
    {
        this.key = key;
        this.value = value;
    }

    public K getKey()
    {
        return key;
    }

    public V getValue()
    {
        return value;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof Pair<?, ?> pair)) return false;

        // making the assumption that 2 pairs will be the same if their keys are the same (used for .contains methods)
        return Objects.equals(key, pair.key);
    }
}
