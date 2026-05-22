package dsa;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  MODULE 7 — LINKED LIST PATTERNS                                         │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * THE FIVE BUILDING BLOCKS YOU WILL REUSE EVERYWHERE
 *
 *   1. DUMMY HEAD     — `ListNode dummy = new ListNode(0); dummy.next = head;`
 *                       Lets you treat removal of the head uniformly.
 *
 *   2. TWO POINTERS   — slow/fast for cycle, middle, nth-from-end.
 *
 *   3. REVERSE IN PLACE — `prev`, `cur`, save `nxt`, flip pointer, advance.
 *
 *   4. MERGE TWO LISTS — dummy head + tail pointer; pick the smaller front each time.
 *
 *   5. SPLIT THE LIST  — find the middle (or kth) and cut.
 *
 * Worked problems in this file:
 *   1. LC 206  Reverse Linked List
 *   2. LC 21   Merge Two Sorted Lists
 *   3. LC 19   Remove Nth Node From End          (dummy + two pointer)
 *   4. LC 142  Linked List Cycle II              (Floyd, find cycle start)
 *   5. LC 92   Reverse Linked List II            (reverse a sub-list)
 *   6. LC 25   Reverse Nodes in K-Group
 *   7. LC 234  Palindrome Linked List            (middle + reverse + compare)
 *   8. LC 143  Reorder List                       (middle + reverse + merge)
 */
public class Module07_LinkedList {

    static class ListNode {
        int val; ListNode next;
        ListNode() {}
        ListNode(int v) { val = v; }
        ListNode(int v, ListNode n) { val = v; next = n; }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1. LC 206 — Reverse Linked List (iterative)
    // ─────────────────────────────────────────────────────────────────────────
    static ListNode reverse(ListNode head) {
        ListNode prev = null, cur = head;
        while (cur != null) {
            ListNode nxt = cur.next;
            cur.next = prev;
            prev = cur;
            cur = nxt;
        }
        return prev;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. LC 21 — Merge Two Sorted Lists
    // ─────────────────────────────────────────────────────────────────────────
    static ListNode mergeTwo(ListNode a, ListNode b) {
        ListNode dummy = new ListNode(0), tail = dummy;
        while (a != null && b != null) {
            if (a.val <= b.val) { tail.next = a; a = a.next; }
            else                { tail.next = b; b = b.next; }
            tail = tail.next;
        }
        tail.next = (a != null) ? a : b;
        return dummy.next;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. LC 19 — Remove Nth Node From End
    //    Advance `fast` n steps. Then move slow & fast together until fast.next == null.
    // ─────────────────────────────────────────────────────────────────────────
    static ListNode removeNthFromEnd(ListNode head, int n) {
        ListNode dummy = new ListNode(0, head);
        ListNode slow = dummy, fast = dummy;
        for (int i = 0; i < n; i++) fast = fast.next;
        while (fast.next != null) { slow = slow.next; fast = fast.next; }
        slow.next = slow.next.next;
        return dummy.next;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. LC 142 — Linked List Cycle II (Floyd's tortoise-and-hare)
    //    Phase 1: meet inside cycle. Phase 2: reset slow to head, advance both
    //    by one — they meet at the cycle's entrance (math: 2(a+b)=a+b+c+b ⇒ a=c).
    // ─────────────────────────────────────────────────────────────────────────
    static ListNode detectCycle(ListNode head) {
        ListNode slow = head, fast = head;
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
            if (slow == fast) {
                ListNode p = head;
                while (p != slow) { p = p.next; slow = slow.next; }
                return p;
            }
        }
        return null;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. LC 92 — Reverse Linked List II (positions [left, right])
    //    Park `prev` just before `left`. Repeatedly hoist the node after `cur`
    //    to position prev.next (in-place head-insertion within the sub-range).
    // ─────────────────────────────────────────────────────────────────────────
    static ListNode reverseBetween(ListNode head, int left, int right) {
        ListNode dummy = new ListNode(0, head);
        ListNode prev = dummy;
        for (int i = 1; i < left; i++) prev = prev.next;
        ListNode cur = prev.next;
        for (int i = 0; i < right - left; i++) {
            ListNode mv = cur.next;
            cur.next = mv.next;
            mv.next = prev.next;
            prev.next = mv;
        }
        return dummy.next;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. LC 25 — Reverse Nodes in K-Group
    //    Walk the list group by group; only reverse if k remaining nodes exist.
    // ─────────────────────────────────────────────────────────────────────────
    static ListNode reverseKGroup(ListNode head, int k) {
        ListNode dummy = new ListNode(0, head);
        ListNode groupPrev = dummy;
        while (true) {
            ListNode kth = groupPrev;
            for (int i = 0; i < k && kth != null; i++) kth = kth.next;
            if (kth == null) break;
            ListNode groupNext = kth.next;
            // reverse [groupPrev.next .. kth]
            ListNode prev = groupNext, cur = groupPrev.next;
            while (cur != groupNext) {
                ListNode nxt = cur.next;
                cur.next = prev;
                prev = cur;
                cur = nxt;
            }
            ListNode newTail = groupPrev.next;
            groupPrev.next = kth;
            groupPrev = newTail;
        }
        return dummy.next;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 7. LC 234 — Palindrome Linked List
    //    Find middle, reverse second half, compare two halves.
    // ─────────────────────────────────────────────────────────────────────────
    static boolean isPalindrome(ListNode head) {
        ListNode slow = head, fast = head;
        while (fast != null && fast.next != null) { slow = slow.next; fast = fast.next.next; }
        ListNode second = reverse(slow);
        ListNode p1 = head, p2 = second;
        while (p2 != null) {
            if (p1.val != p2.val) return false;
            p1 = p1.next; p2 = p2.next;
        }
        return true;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 8. LC 143 — Reorder List   L0→L1→…→Ln-1  becomes  L0→Ln-1→L1→Ln-2…
    //    Find middle, split, reverse second half, weave.
    // ─────────────────────────────────────────────────────────────────────────
    static void reorderList(ListNode head) {
        if (head == null || head.next == null) return;
        ListNode slow = head, fast = head;
        while (fast.next != null && fast.next.next != null) { slow = slow.next; fast = fast.next.next; }
        ListNode second = reverse(slow.next);
        slow.next = null;
        ListNode p1 = head, p2 = second;
        while (p2 != null) {
            ListNode n1 = p1.next, n2 = p2.next;
            p1.next = p2; p2.next = n1;
            p1 = n1; p2 = n2;
        }
    }

    // ── helpers ─────────────────────────────────────────────────────────────
    static ListNode build(int... vs) {
        ListNode dummy = new ListNode(), t = dummy;
        for (int v : vs) { t.next = new ListNode(v); t = t.next; }
        return dummy.next;
    }
    static String show(ListNode h) {
        StringBuilder sb = new StringBuilder("[");
        for (ListNode p = h; p != null; p = p.next) {
            if (sb.length() > 1) sb.append(',');
            sb.append(p.val);
        }
        return sb.append(']').toString();
    }

    public static void main(String[] args) {
        System.out.println("reverse([1..5])         = " + show(reverse(build(1, 2, 3, 4, 5))));
        System.out.println("merge([1,2,4]+[1,3,4]) = " + show(mergeTwo(build(1, 2, 4), build(1, 3, 4))));
        System.out.println("removeNth([1..5], 2)    = " + show(removeNthFromEnd(build(1, 2, 3, 4, 5), 2)));
        System.out.println("reverseBetween(1..5,2,4)= " + show(reverseBetween(build(1, 2, 3, 4, 5), 2, 4)));
        System.out.println("reverseKGroup(1..5, 2)  = " + show(reverseKGroup(build(1, 2, 3, 4, 5), 2)));
        System.out.println("isPalindrome([1,2,2,1])= " + isPalindrome(build(1, 2, 2, 1)));

        ListNode r = build(1, 2, 3, 4, 5);
        reorderList(r);
        System.out.println("reorder([1..5])         = " + show(r));
    }

    /*
     * PRACTICE SET
     *   • LC 2    Add Two Numbers
     *   • LC 23   Merge K Sorted Lists                  (heap)
     *   • LC 86   Partition List
     *   • LC 138  Copy List with Random Pointer
     *   • LC 141  Linked List Cycle (boolean — already in Module 1)
     *   • LC 148  Sort List                              (merge sort on linked list)
     *   • LC 160  Intersection of Two Linked Lists
     *   • LC 328  Odd Even Linked List
     *   • LC 1721 Swapping Nodes in a Linked List
     */
}
