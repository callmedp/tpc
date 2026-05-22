package collections.overview;

import java.util.*;
import java.util.concurrent.*;

/**
 * Demonstrates the Collections Framework hierarchy with one instance per major
 * interface and shows that they share common methods declared in Collection / Map.
 */
public class CollectionsHierarchyExample {

    public static void main(String[] args) {

        // ---------- Collection branch ----------

        // List — ordered, allows duplicates, index-based access
        List<String> list = new ArrayList<>();
        list.add("a"); list.add("b"); list.add("a");
        System.out.println("List: " + list + " (duplicates kept, order preserved)");

        // Set — unique elements
        Set<String> set = new HashSet<>();
        set.add("a"); set.add("b"); set.add("a");
        System.out.println("Set: " + set + " (duplicates dropped)");

        // SortedSet — sorted in natural / comparator order
        SortedSet<String> sortedSet = new TreeSet<>();
        sortedSet.add("c"); sortedSet.add("a"); sortedSet.add("b");
        System.out.println("SortedSet: " + sortedSet);

        // Queue — typically FIFO
        Queue<String> queue = new LinkedList<>();
        queue.offer("first"); queue.offer("second");
        System.out.println("Queue poll: " + queue.poll());

        // Deque — double-ended
        Deque<String> deque = new ArrayDeque<>();
        deque.offerFirst("middle");
        deque.offerFirst("front");
        deque.offerLast("back");
        System.out.println("Deque: " + deque);

        // ---------- Map branch (NOT a Collection) ----------
        Map<String, Integer> map = new HashMap<>();
        map.put("apple", 1); map.put("banana", 2);
        System.out.println("Map: " + map);

        SortedMap<String, Integer> sortedMap = new TreeMap<>(map);
        System.out.println("SortedMap (keys sorted): " + sortedMap);

        // ---------- Common operations defined on Collection ----------
        Collection<String> any = list;
        System.out.println("size=" + any.size() + ", contains 'a'=" + any.contains("a"));

        // ---------- Concurrent / thread-safe variants ----------
        Map<String, Integer> concurrent = new ConcurrentHashMap<>();
        concurrent.put("x", 10);
        List<String> cow = new CopyOnWriteArrayList<>(List.of("p", "q", "r"));
        System.out.println("Concurrent map: " + concurrent + ", COW list: " + cow);
    }
}