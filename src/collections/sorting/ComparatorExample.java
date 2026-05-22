package collections.sorting;

import java.util.*;

/**
 * Comparator — external orderings. All Java 8+ factory methods demonstrated.
 */
public class ComparatorExample {

    static record Emp(String name, String dept, int salary, Integer bonus) {}

    public static void main(String[] args) {
        List<Emp> emps = new ArrayList<>(List.of(
            new Emp("Alice", "ENG",  90_000, 5000),
            new Emp("Bob",   "ENG",  90_000, null),
            new Emp("Carol", "OPS",  70_000, 3000),
            new Emp("Dave",  "OPS",  80_000, 2000),
            new Emp("Eve",   "ENG",  80_000, 1000)
        ));

        // ---------- Single-key ----------
        emps.sort(Comparator.comparing(Emp::name));
        System.out.println("by name: " + emps);

        emps.sort(Comparator.comparingInt(Emp::salary));
        System.out.println("by salary: " + emps);

        // ---------- Reversed ----------
        emps.sort(Comparator.comparingInt(Emp::salary).reversed());
        System.out.println("by salary desc: " + emps);

        // ---------- Multi-key — dept asc, then salary desc ----------
        Comparator<Emp> byDeptThenSalDesc =
            Comparator.comparing(Emp::dept)
                      .thenComparing(Emp::salary, Comparator.reverseOrder());
        emps.sort(byDeptThenSalDesc);
        System.out.println("dept then salary desc:");
        emps.forEach(e -> System.out.println("  " + e));

        // ---------- Nulls handling ----------
        emps.sort(Comparator.comparing(Emp::bonus, Comparator.nullsLast(Comparator.naturalOrder())));
        System.out.println("by bonus (nulls last): " + emps);

        // ---------- Natural / reverse for plain types ----------
        List<Integer> nums = new ArrayList<>(List.of(3,1,4,1,5,9,2,6));
        nums.sort(Comparator.naturalOrder());
        System.out.println("nums asc: " + nums);
        nums.sort(Comparator.reverseOrder());
        System.out.println("nums desc: " + nums);

        // ---------- Anonymous / lambda Comparator ----------
        Comparator<String> byLength = (a, b) -> Integer.compare(a.length(), b.length());
        List<String> words = new ArrayList<>(List.of("kiwi", "fig", "banana", "apple"));
        words.sort(byLength.thenComparing(Comparator.naturalOrder()));
        System.out.println("by length then alpha: " + words);

        // ---------- TreeSet/TreeMap with custom comparator ----------
        TreeSet<Emp> bySalAsc = new TreeSet<>(Comparator.comparingInt(Emp::salary));
        bySalAsc.addAll(emps);
        // CAUTION: same-salary employees considered equal -> dedupes!
        System.out.println("treeset by salary (note dedup): " + bySalAsc);

        // Make it a strict order by adding tiebreaker
        TreeSet<Emp> bySalThenName = new TreeSet<>(
            Comparator.comparingInt(Emp::salary).thenComparing(Emp::name));
        bySalThenName.addAll(emps);
        System.out.println("treeset by salary+name: " + bySalThenName);
    }
}