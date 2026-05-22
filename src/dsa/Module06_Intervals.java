package dsa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  MODULE 6 — INTERVALS / SWEEP LINE                                       │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * WHEN TO RECOGNIZE
 *   • Input is a LIST OF INTERVALS [start, end].
 *   • Asked to: merge, insert, find overlaps, count concurrent, schedule
 *     non-overlapping, find min rooms, free time, etc.
 *
 * THE CORE IDEAS
 *   (A) SORT — almost always sort by start (sometimes by end for greedy).
 *   (B) MERGE — keep a running interval; extend or push when scanning.
 *   (C) MIN-HEAP OF END TIMES — for concurrency / room-count problems.
 *   (D) EVENT SWEEP — separate +1 (start) / -1 (end) events; scan in time order.
 *
 * Worked problems in this file:
 *   1. LC 56   Merge Intervals
 *   2. LC 57   Insert Interval
 *   3. LC 252  Meeting Rooms (boolean: can attend all?)
 *   4. LC 253  Meeting Rooms II (min rooms — heap & sweep)
 *   5. LC 435  Non-overlapping Intervals (greedy by END)
 *   6. LC 452  Minimum Number of Arrows                (greedy by END)
 *   7. LC 1288 Remove Covered Intervals
 */
public class Module06_Intervals {

    // ─────────────────────────────────────────────────────────────────────────
    // 1. LC 56 — Merge Intervals
    // ─────────────────────────────────────────────────────────────────────────
    static int[][] merge(int[][] intervals) {
        Arrays.sort(intervals, Comparator.comparingInt(x -> x[0]));
        List<int[]> out = new ArrayList<>();
        int[] cur = intervals[0];
        for (int i = 1; i < intervals.length; i++) {
            if (intervals[i][0] <= cur[1]) {                // overlap
                cur[1] = Math.max(cur[1], intervals[i][1]);
            } else {
                out.add(cur);
                cur = intervals[i];
            }
        }
        out.add(cur);
        return out.toArray(new int[0][]);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. LC 57 — Insert Interval (input already sorted, no overlap)
    //    Three phases: before, overlapping (merged), after.
    // ─────────────────────────────────────────────────────────────────────────
    static int[][] insert(int[][] intervals, int[] ni) {
        List<int[]> out = new ArrayList<>();
        int i = 0, n = intervals.length;
        while (i < n && intervals[i][1] < ni[0]) out.add(intervals[i++]);   // before
        while (i < n && intervals[i][0] <= ni[1]) {                          // overlap
            ni[0] = Math.min(ni[0], intervals[i][0]);
            ni[1] = Math.max(ni[1], intervals[i][1]);
            i++;
        }
        out.add(ni);
        while (i < n) out.add(intervals[i++]);                               // after
        return out.toArray(new int[0][]);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. LC 252 — Meeting Rooms (can attend all?)
    // ─────────────────────────────────────────────────────────────────────────
    static boolean canAttendAll(int[][] m) {
        Arrays.sort(m, Comparator.comparingInt(x -> x[0]));
        for (int i = 1; i < m.length; i++) if (m[i][0] < m[i - 1][1]) return false;
        return true;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. LC 253 — Meeting Rooms II (min rooms)
    //    Min-heap of END times; reuse a room iff its end ≤ current start.
    // ─────────────────────────────────────────────────────────────────────────
    static int minMeetingRooms(int[][] m) {
        Arrays.sort(m, Comparator.comparingInt(x -> x[0]));
        PriorityQueue<Integer> ends = new PriorityQueue<>();
        for (int[] mt : m) {
            if (!ends.isEmpty() && ends.peek() <= mt[0]) ends.poll();
            ends.offer(mt[1]);
        }
        return ends.size();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  alt: SWEEP LINE for LC 253 — when you also want a time-by-time chart.
    //    Sort starts and ends separately. Walk two pointers.
    // ─────────────────────────────────────────────────────────────────────────
    static int minMeetingRoomsSweep(int[][] m) {
        int n = m.length;
        int[] starts = new int[n], ends = new int[n];
        for (int i = 0; i < n; i++) { starts[i] = m[i][0]; ends[i] = m[i][1]; }
        Arrays.sort(starts); Arrays.sort(ends);
        int rooms = 0, peak = 0, j = 0;
        for (int i = 0; i < n; i++) {
            if (starts[i] < ends[j]) rooms++;
            else { j++; }                                       // a meeting ended first
            peak = Math.max(peak, rooms);
        }
        return peak;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. LC 435 — Erase Min Intervals to Make Non-overlapping
    //    Greedy: sort by END. Keep an interval iff its start ≥ last kept end.
    // ─────────────────────────────────────────────────────────────────────────
    static int eraseOverlapIntervals(int[][] intervals) {
        Arrays.sort(intervals, Comparator.comparingInt(x -> x[1]));
        int kept = 0, lastEnd = Integer.MIN_VALUE;
        for (int[] in : intervals) {
            if (in[0] >= lastEnd) { kept++; lastEnd = in[1]; }
        }
        return intervals.length - kept;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. LC 452 — Minimum Number of Arrows (greedy by END)
    //    Same shape: shoot the arrow at the END of the earliest-ending balloon;
    //    pop any balloon whose start ≤ that arrow.
    // ─────────────────────────────────────────────────────────────────────────
    static int findMinArrowShots(int[][] points) {
        Arrays.sort(points, Comparator.comparingInt(x -> x[1]));
        int arrows = 0, arrow = Integer.MIN_VALUE;
        for (int[] p : points) {
            if (arrows == 0 || p[0] > arrow) {            // need a new arrow
                arrows++;
                arrow = p[1];
            }
        }
        return arrows;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 7. LC 1288 — Remove Covered Intervals
    //    Sort by start ASC, end DESC. Keep an interval iff its end > running max end.
    // ─────────────────────────────────────────────────────────────────────────
    static int removeCoveredIntervals(int[][] intervals) {
        Arrays.sort(intervals,
                (a, b) -> a[0] != b[0] ? a[0] - b[0] : b[1] - a[1]);
        int kept = 0, runEnd = 0;
        for (int[] in : intervals) {
            if (in[1] > runEnd) { kept++; runEnd = in[1]; }
        }
        return kept;
    }

    public static void main(String[] args) {
        int[][] iv = {{1, 3}, {2, 6}, {8, 10}, {15, 18}};
        System.out.println("merge          = " + Arrays.deepToString(merge(iv)));
        System.out.println("insert([[1,3],[6,9]], [2,5]) = "
                + Arrays.deepToString(insert(new int[][]{{1, 3}, {6, 9}}, new int[]{2, 5})));
        int[][] m = {{0, 30}, {5, 10}, {15, 20}};
        System.out.println("canAttendAll   = " + canAttendAll(m));
        System.out.println("minRooms (heap)= " + minMeetingRooms(m));
        System.out.println("minRooms(sweep)= " + minMeetingRoomsSweep(m));
        System.out.println("eraseOverlap   = " + eraseOverlapIntervals(new int[][]{{1, 2}, {2, 3}, {3, 4}, {1, 3}}));
        System.out.println("minArrows      = " + findMinArrowShots(new int[][]{{10, 16}, {2, 8}, {1, 6}, {7, 12}}));
        System.out.println("removeCovered  = " + removeCoveredIntervals(new int[][]{{1, 4}, {3, 6}, {2, 8}}));
    }

    /*
     * PRACTICE SET
     *   • LC 759    Employee Free Time
     *   • LC 986    Interval List Intersections
     *   • LC 1851   Minimum Interval to Include Each Query
     *   • LC 218    The Skyline Problem            (sweep + multiset)
     *   • LC 56→57→253 are MUST-KNOW for big-tech onsite.
     *   • LC 1094   Car Pooling                    (difference array — see Module 3)
     *   • LC 729    My Calendar I                  (TreeMap floor/ceiling — collections.dsa M7)
     */
}
