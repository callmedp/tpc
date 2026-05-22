package collections.dsa;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Map;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  MODULE 9 — STACK (via ArrayDeque)                                       │
 * │  Prereq:  Modules 1–3                                                    │
 * │  Goal:    DFS-iterative, monotonic stack, parentheses, expression eval    │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 *   Use  ArrayDeque  as a stack.  NEVER use java.util.Stack — it's a
 *   legacy synchronized class that subclasses Vector and is slow.
 *
 *   Stack API on Deque:
 *       push(e)     — add to top      (== addFirst)
 *       pop()       — remove from top (== removeFirst, throws if empty)
 *       peek()      — look at top, return null if empty
 *       isEmpty / size
 *
 * What you'll learn:
 *   PATTERN 1 — Matching: valid parentheses (LC 20)
 *   PATTERN 2 — Monotonic stack: next greater element (LC 496/503/739)
 *   PATTERN 3 — Expression evaluation: reverse-polish (LC 150), basic calc (LC 224)
 *   PATTERN 4 — DFS-iterative: tree/graph traversal without recursion
 *   PATTERN 5 — Stack with min: design problem (LC 155)
 */
public class Module09_Stack {

    public static void main(String[] args) {

        // ─────────────────────────────────────────────────────────────────────
        // 1. The five methods you'll use 95% of the time
        // ─────────────────────────────────────────────────────────────────────
        Deque<Integer> st = new ArrayDeque<>();
        st.push(1);                   // [1]
        st.push(2);                   // [2, 1]   (top first)
        st.push(3);                   // [3, 2, 1]
        st.peek();                    // 3        (no remove)
        st.pop();                     // 3        (removes)
        st.size();   st.isEmpty();
        // (push == addFirst, pop == removeFirst, peek == peekFirst)

        // ─────────────────────────────────────────────────────────────────────
        // 2. PATTERN 1 — Valid Parentheses (LC 20)
        // ─────────────────────────────────────────────────────────────────────
        String expr = "({[]})";
        Map<Character, Character> pair = Map.of(')', '(', ']', '[', '}', '{');
        Deque<Character> brackets = new ArrayDeque<>();
        boolean valid = true;
        for (char c : expr.toCharArray()) {
            if (pair.containsValue(c)) brackets.push(c);                   // open
            else if (brackets.isEmpty() || brackets.pop() != pair.get(c)) {
                valid = false; break;
            }
        }
        if (!brackets.isEmpty()) valid = false;
        System.out.println("\"" + expr + "\" valid=" + valid);

        // ─────────────────────────────────────────────────────────────────────
        // 3. PATTERN 2 — Monotonic stack: Next Greater Element (LC 496/739)
        //    Invariant: stack holds INDICES whose values form a DECREASING sequence.
        //    On each new element, pop everything smaller — those indices' answers = current.
        // ─────────────────────────────────────────────────────────────────────
        int[] nums = {2, 1, 2, 4, 3};
        int[] nge  = new int[nums.length];
        Arrays.fill(nge, -1);
        Deque<Integer> mono = new ArrayDeque<>();
        for (int i = 0; i < nums.length; i++) {
            while (!mono.isEmpty() && nums[mono.peek()] < nums[i]) {
                nge[mono.pop()] = nums[i];
            }
            mono.push(i);
        }
        System.out.println("nextGreater = " + Arrays.toString(nge));

        // ─────────────────────────────────────────────────────────────────────
        // 4. PATTERN 3 — Evaluate Reverse Polish Notation (LC 150)
        // ─────────────────────────────────────────────────────────────────────
        String[] tokens = {"2", "1", "+", "3", "*"};
        Deque<Integer> calc = new ArrayDeque<>();
        for (String t : tokens) {
            switch (t) {
                case "+", "-", "*", "/" -> {
                    int b = calc.pop(), a = calc.pop();
                    calc.push(switch (t) {
                        case "+" -> a + b;
                        case "-" -> a - b;
                        case "*" -> a * b;
                        default  -> a / b;
                    });
                }
                default -> calc.push(Integer.parseInt(t));
            }
        }
        System.out.println("RPN result  = " + calc.pop());                  // 9

        // ─────────────────────────────────────────────────────────────────────
        // 5. PATTERN 4 — DFS-iterative skeleton (preorder)
        //    (Real tree nodes omitted; this shows the shape.)
        // ─────────────────────────────────────────────────────────────────────
        Deque<int[]> dfs = new ArrayDeque<>();
        boolean[][] visited = new boolean[3][3];
        dfs.push(new int[]{0, 0});
        while (!dfs.isEmpty()) {
            int[] cell = dfs.pop();
            int r = cell[0], c = cell[1];
            if (r < 0 || r >= 3 || c < 0 || c >= 3 || visited[r][c]) continue;
            visited[r][c] = true;
            dfs.push(new int[]{r + 1, c});
            dfs.push(new int[]{r, c + 1});
        }

        // ─────────────────────────────────────────────────────────────────────
        // 6. PATTERN 5 — Min Stack (LC 155)
        //    Maintain a parallel stack of running minima.  O(1) min query.
        // ─────────────────────────────────────────────────────────────────────
        MinStack ms = new MinStack();
        ms.push(3); ms.push(5); ms.push(2); ms.push(1);
        System.out.println("min so far  = " + ms.min());                    // 1
        ms.pop();
        System.out.println("min now     = " + ms.min());                    // 2

        // ─────────────────────────────────────────────────────────────────────
        // Practice problems:
        //   • LC 20    Valid Parentheses
        //   • LC 155   Min Stack
        //   • LC 150   Evaluate RPN
        //   • LC 224   Basic Calculator (with parentheses)
        //   • LC 496   Next Greater Element I
        //   • LC 739   Daily Temperatures
        //   • LC 84    Largest Rectangle in Histogram
        //   • LC 394   Decode String
        // ─────────────────────────────────────────────────────────────────────
    }

    /** Min Stack — O(1) push/pop/min. */
    static class MinStack {
        private final Deque<Integer> data = new ArrayDeque<>();
        private final Deque<Integer> mins = new ArrayDeque<>();
        void push(int x) {
            data.push(x);
            mins.push(mins.isEmpty() ? x : Math.min(mins.peek(), x));
        }
        void pop() { data.pop(); mins.pop(); }
        int top()  { return data.peek(); }
        int min()  { return mins.peek(); }
    }
}