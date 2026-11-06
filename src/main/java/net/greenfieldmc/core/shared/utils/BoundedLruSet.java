package net.greenfieldmc.core.shared.utils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A thread-safe set that holds at most a fixed number of entries. Once it is full, adding an entry drops the entry
 * that was used (added or checked with {@link #contains}) longest ago.
 * @param <T> The type of the entries
 */
public class BoundedLruSet<T> {

    private final Map<T, Boolean> entries;

    public BoundedLruSet(int maxEntries) {
        // access ordered, so contains() keeps an entry from being the next one evicted
        this.entries = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<T, Boolean> eldest) {
                return size() > maxEntries;
            }
        };
    }

    public synchronized boolean contains(T entry) {
        return entries.get(entry) != null;
    }

    public synchronized void add(T entry) {
        entries.put(entry, Boolean.TRUE);
    }

    public synchronized void remove(T entry) {
        entries.remove(entry);
    }

    public synchronized void clear() {
        entries.clear();
    }

}
