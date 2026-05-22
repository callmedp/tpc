package collections.list;

import java.util.*;

/**
 * Vector and Stack — LEGACY synchronized classes.
 * Prefer ArrayList (single-threaded) or CopyOnWriteArrayList /
 * Collections.synchronizedList for concurrent use. Prefer ArrayDeque for stack.
 */
public class VectorStackExample {

    public static void main(String[] args) {

        // ---------- Vector ----------
        Vector<Integer> v = new Vector<>(4, 2);   // initial capacity 4, increment 2
        v.add(1); v.add(2); v.add(3);
        v.addElement(4);                          // legacy method (== add)
        System.out.println("vector: " + v);
        System.out.println("capacity: " + v.capacity() + ", size: " + v.size());

        // Enumeration — legacy iterator (NO remove)
        Enumeration<Integer> en = v.elements();
        while (en.hasMoreElements()) System.out.print(en.nextElement() + " ");
        System.out.println();

        // ---------- Stack (extends Vector) — AVOID, use ArrayDeque ----------
        Stack<String> s = new Stack<>();
        s.push("a"); s.push("b"); s.push("c");
        System.out.println("top: " + s.peek());        // c
        System.out.println("pop: " + s.pop());         // c
        System.out.println("search b: " + s.search("b")); // 1-based distance from top, -1 if absent
        System.out.println("after pop: " + s);

        // ---------- Preferred replacement ----------
        Deque<String> modernStack = new ArrayDeque<>();
        modernStack.push("a"); modernStack.push("b");
        System.out.println("ArrayDeque stack peek: " + modernStack.peek());
    }
}