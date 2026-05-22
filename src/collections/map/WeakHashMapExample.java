package collections.map;

import java.util.*;

/**
 * WeakHashMap — keys held by weak references. When a key has no strong refs,
 * GC may reclaim it and the entry disappears on the next map access.
 *
 * Use case: per-instance metadata cache where you don't control object lifetime.
 */
public class WeakHashMapExample {

    public static void main(String[] args) throws InterruptedException {

        WeakHashMap<Object, String> w = new WeakHashMap<>();

        Object key1 = new Object();
        Object key2 = new Object();
        w.put(key1, "meta-1");
        w.put(key2, "meta-2");
        System.out.println("before drop: size=" + w.size());

        // Drop strong reference to key1; key2 still strongly referenced
        key1 = null;

        // Hint the GC and wait — best effort (not guaranteed in real programs)
        for (int i = 0; i < 5 && w.size() > 1; i++) {
            System.gc();
            Thread.sleep(50);
        }

        System.out.println("after GC: size=" + w.size());          // likely 1
        System.out.println("remaining: " + w);

        // ---------- Anti-pattern ----------
        // If the value strongly references its key, the key never becomes weak-only:
        //   WeakHashMap<Node, Node> graph;  graph.put(n, n);  // n stays alive forever
        // Workaround: WeakHashMap<K, WeakReference<V>>.
    }
}