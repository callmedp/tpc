package collections.list;

import java.util.*;

/**
 * LinkedList — doubly linked list. Implements List AND Deque.
 * O(1) head/tail ops; O(n) random access; high memory overhead per node.
 */
public class LinkedListExample {

    public static void main(String[] args) {
        LinkedList<Integer> ll = new LinkedList<>();

        // ---------- As a List ----------
        ll.add(1); ll.add(2); ll.add(3);
        ll.add(1, 99);                       // insert at index — O(n) walk + O(1) link
        System.out.println("list view: " + ll);

        // ---------- As a Deque / Queue ----------
        ll.addFirst(0);                      // O(1)
        ll.addLast(100);                     // O(1)
        ll.offer(200);                       // offer == addLast for FIFO
        System.out.println("deque view: " + ll);

        System.out.println("peek (head): " + ll.peek());           // null if empty
        System.out.println("peekFirst:  " + ll.peekFirst());
        System.out.println("peekLast:   " + ll.peekLast());
        System.out.println("getFirst:   " + ll.getFirst());        // throws if empty
        System.out.println("getLast:    " + ll.getLast());

        // ---------- Remove ops ----------
        System.out.println("poll: " + ll.poll());           // removes head, null if empty
        System.out.println("pollFirst: " + ll.pollFirst());
        System.out.println("pollLast: " + ll.pollLast());
        System.out.println("after polls: " + ll);

        // ---------- Stack ops (push to head, pop from head) ----------
        Deque<String> stack = new LinkedList<>();
        stack.push("a"); stack.push("b"); stack.push("c");
        System.out.println("stack top: " + stack.peek());     // c
        System.out.println("pop: " + stack.pop());            // c
        System.out.println("after pop: " + stack);

        // ---------- Descending iteration ----------
        LinkedList<Integer> d = new LinkedList<>(List.of(1,2,3,4));
        Iterator<Integer> desc = d.descendingIterator();
        System.out.print("descending: ");
        while (desc.hasNext()) System.out.print(desc.next() + " ");
        System.out.println();

        // ---------- Performance trap: indexed loop on LinkedList ----------
        // BAD: O(n^2) — each get(i) walks from head
        // for (int i = 0; i < d.size(); i++) System.out.println(d.get(i));
        // GOOD: O(n)
        for (int v : d) { /* ... */ }
    }
}