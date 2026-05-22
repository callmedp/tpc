package collections.map;

import java.util.*;

/**
 * IdentityHashMap — uses == (reference equality) and System.identityHashCode(k)
 * instead of equals / hashCode. Distinct String objects with same content are
 * different keys.
 *
 * Use cases: object-graph traversal, serialization frameworks, cycle detection.
 */
public class IdentityHashMapExample {

    public static void main(String[] args) {

        // Two String objects with the same content but different references
        String a = new String("hello");
        String b = new String("hello");
        System.out.println("a.equals(b) = " + a.equals(b));   // true
        System.out.println("a == b     = " + (a == b));        // false

        // Regular HashMap: a and b are the SAME key
        HashMap<String, Integer> hm = new HashMap<>();
        hm.put(a, 1); hm.put(b, 2);
        System.out.println("HashMap size: " + hm.size());      // 1

        // IdentityHashMap: a and b are DIFFERENT keys
        IdentityHashMap<String, Integer> idm = new IdentityHashMap<>();
        idm.put(a, 1); idm.put(b, 2);
        System.out.println("IdentityHashMap size: " + idm.size()); // 2

        // ---------- Cycle / visited-set pattern ----------
        Map<Object, Boolean> visited = new IdentityHashMap<>();
        Object obj = new Object();
        visited.put(obj, Boolean.TRUE);
        System.out.println("visited? " + visited.containsKey(obj));
    }
}