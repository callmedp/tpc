package collections.dsa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  MODULE 3 — LISTS (ArrayList, LinkedList)                                │
 * │  Prereq:  Modules 1–2                                                    │
 * │  Goal:    your default sequential container — when & how to use it       │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * For 99% of DSA problems use ArrayList. LinkedList is rare in interviews
 * (you usually implement linked lists by hand) but its Deque methods do show
 * up. We cover both here so the comparison is concrete.
 *
 * What you'll learn:
 *   • add / add(idx) / set / get / remove(idx) vs remove(Object)
 *   • indexOf / lastIndexOf / contains / size / isEmpty / clear
 *   • subList                    — VIEW semantics & the classic trap
 *   • sort / replaceAll / removeIf
 *   • toArray (with type)        — the right way
 *   • Iterator / ListIterator    — safe in-place removal and bidirectional traversal
 *   • List.of / List.copyOf      — immutable factories
 *   • LinkedList as Deque        — addFirst/Last, removeFirst/Last (preview of Module 9–10)
 *   • Complexity table at the bottom
 */
public class Module03_Lists {

    public static void main(String[] args) {

        // ─────────────────────────────────────────────────────────────────────
        // 1. Creating
        // ─────────────────────────────────────────────────────────────────────
        List<Integer> list = new ArrayList<>();
        list.add(3); list.add(1); list.add(4); list.add(1); list.add(5); list.add(9);

        // Quick literal — but be careful: List.of() returns an IMMUTABLE list
        List<Integer> immut = List.of(1, 2, 3);                   // throws on add
        List<Integer> mut   = new ArrayList<>(List.of(1, 2, 3));  // copy → mutable

        // ─────────────────────────────────────────────────────────────────────
        // 2. Read / write / find
        // ─────────────────────────────────────────────────────────────────────
        list.get(0);                          // 3      O(1) for ArrayList
        list.set(0, 30);                      // replace at index 0
        list.size();    list.isEmpty();
        list.contains(5);
        list.indexOf(1);                      // first occurrence
        list.lastIndexOf(1);                  // last occurrence

        // ─────────────────────────────────────────────────────────────────────
        // 3. Insert / remove — beware overload resolution
        // ─────────────────────────────────────────────────────────────────────
        list.add(0, 99);                      // insert at index   O(n)
        list.remove(0);                       // remove BY INDEX   → removes index 0
        list.remove(Integer.valueOf(1));      // remove BY VALUE   → removes first '1'
        // ^^ classic interview gotcha. list.remove(1) does NOT remove the value 1.

        // ─────────────────────────────────────────────────────────────────────
        // 4. Bulk transforms (Java 8)
        // ─────────────────────────────────────────────────────────────────────
        list.replaceAll(x -> x * 10);         // in-place map
        list.removeIf(x -> x > 40);           // in-place filter

        // ─────────────────────────────────────────────────────────────────────
        // 5. subList — VIEW, not a copy.  Modifying the parent INVALIDATES it.
        // ─────────────────────────────────────────────────────────────────────
        List<Integer> base = new ArrayList<>(List.of(10, 20, 30, 40, 50));
        List<Integer> view = base.subList(1, 4);          // [20, 30, 40]
        view.set(0, 200);                                 // mutates BASE too → [10,200,30,40,50]
        // base.add(99); view.get(0); // would throw ConcurrentModificationException

        // Safe snapshot:
        List<Integer> snap = new ArrayList<>(base.subList(1, 4));

        // ─────────────────────────────────────────────────────────────────────
        // 6. Sorting
        // ─────────────────────────────────────────────────────────────────────
        list.sort(Comparator.naturalOrder());
        list.sort(Comparator.reverseOrder());

        // ─────────────────────────────────────────────────────────────────────
        // 7. Conversion: List → array
        // ─────────────────────────────────────────────────────────────────────
        Integer[] arr = list.toArray(new Integer[0]);     // typed array
        int[] prim    = list.stream().mapToInt(Integer::intValue).toArray();   // primitive

        // ─────────────────────────────────────────────────────────────────────
        // 8. Iterator — safe removal during iteration
        //    A normal for-each that calls list.remove() will throw CME.
        // ─────────────────────────────────────────────────────────────────────
        List<Integer> nums = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));
        var it = nums.iterator();
        while (it.hasNext()) {
            if (it.next() % 2 == 0) it.remove();          // SAFE — modifies via iterator
        }
        // (Easier: nums.removeIf(x -> x % 2 == 0))

        // ─────────────────────────────────────────────────────────────────────
        // 9. ListIterator — bidirectional + set/add in place
        // ─────────────────────────────────────────────────────────────────────
        ListIterator<Integer> lit = nums.listIterator();
        while (lit.hasNext()) {
            int idx = lit.nextIndex();
            int v   = lit.next();
            lit.set(v * 10);                              // replace current
            if (idx == 0) lit.add(-1);                    // insert after current position
        }
        while (lit.hasPrevious()) {                       // reverse traversal
            lit.previous();
        }

        // ─────────────────────────────────────────────────────────────────────
        // 10. Collections helpers that operate on Lists
        //     (full coverage in Module 13)
        // ─────────────────────────────────────────────────────────────────────
        Collections.reverse(nums);
        Collections.shuffle(nums);
        Collections.swap(nums, 0, nums.size() - 1);
        Collections.rotate(nums, 2);                      // shift right by 2
        Collections.fill(nums, 0);                        // overwrite all with 0

        // ─────────────────────────────────────────────────────────────────────
        // 11. LinkedList — when (rarely) does it win?
        //     • Frequent inserts/removes at BOTH ends    → use as Deque
        //     • Random access by index is O(n)           → NOT good for `list.get(i)` loops
        //     • Implements Deque, so works as stack/queue
        // ─────────────────────────────────────────────────────────────────────
        LinkedList<Integer> ll = new LinkedList<>();
        ll.addFirst(1);     ll.addLast(2);
        ll.removeFirst();   ll.removeLast();
        ll.peekFirst();     ll.peekLast();

        // ─────────────────────────────────────────────────────────────────────
        // 12. Complexity cheatsheet (operation → ArrayList / LinkedList)
        //     get(i)         O(1)     O(n)
        //     add(end)       amort O(1)  O(1)
        //     add(0)         O(n)     O(1)
        //     remove(0)      O(n)     O(1)
        //     contains(o)    O(n)     O(n)
        //     iterate full   O(n)     O(n)      (LinkedList cache-unfriendly)
        // ─────────────────────────────────────────────────────────────────────

        System.out.println("list      = " + list);
        System.out.println("base      = " + base);
        System.out.println("view      = " + view);
        System.out.println("snap      = " + snap);
        System.out.println("nums      = " + nums);
        System.out.println("prim len  = " + prim.length);
        System.out.println("arr len   = " + arr.length);

        // ─────────────────────────────────────────────────────────────────────
        // Practice problems:
        //   • LC 1     Two Sum                  (List as result builder)
        //   • LC 15    3Sum                     (sort + two-pointer)
        //   • LC 56    Merge Intervals          (sort by start, scan)
        //   • LC 88    Merge Sorted Array       (in-place using indices)
        //   • LC 27    Remove Element           (in-place — like list.removeIf)
        // ─────────────────────────────────────────────────────────────────────
    }
}