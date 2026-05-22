package collections.queue;

import java.util.*;

/**
 * ArrayDeque — circular array, double-ended, faster than LinkedList and Stack
 * for queue/stack workloads. Allows no null.
 */
public class ArrayDequeExample {

    public static void main(String[] args) {

        // ---------- As a Queue (FIFO) ----------
        ArrayDeque<String> q = new ArrayDeque<>();
        q.offer("a"); q.offer("b"); q.offer("c");
        System.out.println("queue peek: " + q.peek());     // a
        System.out.println("poll: " + q.poll());           // a
        System.out.println("queue after poll: " + q);

        // ---------- As a Stack (LIFO) ----------
        ArrayDeque<Integer> stack = new ArrayDeque<>();
        stack.push(1); stack.push(2); stack.push(3);
        System.out.println("stack peek: " + stack.peek()); // 3
        System.out.println("pop: " + stack.pop());         // 3
        System.out.println("stack after pop: " + stack);

        // ---------- As a Deque ----------
        ArrayDeque<Integer> d = new ArrayDeque<>();
        d.offerFirst(1);
        d.offerFirst(0);
        d.offerLast(2);
        d.offerLast(3);
        System.out.println("deque: " + d);                 // [0, 1, 2, 3]
        System.out.println("pollFirst: " + d.pollFirst());
        System.out.println("pollLast:  " + d.pollLast());
        System.out.println("after polls: " + d);

        // ---------- Examine both ends ----------
        System.out.println("getFirst: " + d.getFirst());   // throws if empty
        System.out.println("peekLast: " + d.peekLast());   // null if empty

        // ---------- Iterators in both directions ----------
        d.addAll(List.of(10, 20, 30, 40));
        System.out.print("forward: ");
        d.iterator().forEachRemaining(v -> System.out.print(v + " "));
        System.out.println();
        System.out.print("descending: ");
        d.descendingIterator().forEachRemaining(v -> System.out.print(v + " "));
        System.out.println();

        // ---------- Throws/special-value pairs ----------
        ArrayDeque<Integer> e = new ArrayDeque<>();
        try { e.removeFirst(); } catch (NoSuchElementException ex) { System.out.println("removeFirst on empty: throws"); }
        System.out.println("pollFirst on empty: " + e.pollFirst()); // null

        // ---------- null rejected ----------
        try { d.offer(null); }
        catch (NullPointerException ex) { System.out.println("ArrayDeque rejects null"); }
    }
}