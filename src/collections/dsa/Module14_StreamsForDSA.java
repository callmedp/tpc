package collections.dsa;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  MODULE 14 — STREAMS FOR DSA                                             │
 * │  Prereq:  Modules 1–5                                                    │
 * │  Goal:    one-liners with Stream / IntStream / Collectors                │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 *   Most interviews want clean loops. But streams give you:
 *     • compact frequency / grouping
 *     • range-based int loops
 *     • clean array→list / list→map conversions
 *
 *   They are also slower than raw loops — don't write hot inner code in a
 *   stream if perf matters.
 *
 * What you'll learn:
 *   • Arrays.stream / IntStream.of / IntStream.range / rangeClosed
 *   • filter / map / mapToInt / mapToObj / boxed / sorted / distinct / limit / skip
 *   • reduce / count / sum / max / min / average / anyMatch / allMatch / noneMatch
 *   • collect: toList / toSet / toMap / toUnmodifiableList
 *   • Collectors.groupingBy / counting / partitioningBy / joining / mapping / summingInt
 */
public class Module14_StreamsForDSA {

    public static void main(String[] args) {

        // ─────────────────────────────────────────────────────────────────────
        // 1. Sources
        // ─────────────────────────────────────────────────────────────────────
        int[] nums = {3, 1, 4, 1, 5, 9, 2, 6};
        Arrays.stream(nums);                                  // IntStream
        IntStream.of(1, 2, 3);
        IntStream.range(0, 5);                                // 0..4   (exclusive end)
        IntStream.rangeClosed(1, 5);                          // 1..5
        Stream.of("a", "b", "c");
        List.of(1, 2, 3).stream();

        // ─────────────────────────────────────────────────────────────────────
        // 2. Aggregates over int[]
        // ─────────────────────────────────────────────────────────────────────
        int sum = Arrays.stream(nums).sum();
        int max = Arrays.stream(nums).max().getAsInt();
        int min = Arrays.stream(nums).min().getAsInt();
        double avg = Arrays.stream(nums).average().getAsDouble();
        long  cnt = Arrays.stream(nums).count();

        // ─────────────────────────────────────────────────────────────────────
        // 3. Transforms on int[]
        // ─────────────────────────────────────────────────────────────────────
        int[] sorted   = Arrays.stream(nums).sorted().toArray();
        int[] distinct = Arrays.stream(nums).distinct().toArray();
        int[] squares  = Arrays.stream(nums).map(x -> x * x).toArray();
        int[] evens    = Arrays.stream(nums).filter(x -> x % 2 == 0).toArray();

        // ─────────────────────────────────────────────────────────────────────
        // 4. Predicates
        // ─────────────────────────────────────────────────────────────────────
        boolean anyOver5  = Arrays.stream(nums).anyMatch(x -> x > 5);
        boolean allPos    = Arrays.stream(nums).allMatch(x -> x > 0);
        boolean noneNeg   = Arrays.stream(nums).noneMatch(x -> x < 0);

        // ─────────────────────────────────────────────────────────────────────
        // 5. Box / unbox
        // ─────────────────────────────────────────────────────────────────────
        List<Integer> boxed = Arrays.stream(nums).boxed().toList();
        int[] prim = boxed.stream().mapToInt(Integer::intValue).toArray();

        // ─────────────────────────────────────────────────────────────────────
        // 6. Frequency map in one expression
        // ─────────────────────────────────────────────────────────────────────
        List<String> words = List.of("eat", "tea", "tan", "ate", "nat", "bat");
        Map<String, Long> freq = words.stream()
                .collect(Collectors.groupingBy(w -> w, Collectors.counting()));

        // ─────────────────────────────────────────────────────────────────────
        // 7. Group anagrams in one expression (LC 49 in 4 lines)
        // ─────────────────────────────────────────────────────────────────────
        Map<String, List<String>> groups = words.stream()
                .collect(Collectors.groupingBy(w -> {
                    char[] c = w.toCharArray();
                    Arrays.sort(c);
                    return new String(c);
                }));

        // ─────────────────────────────────────────────────────────────────────
        // 8. partitioningBy — binary split into true / false buckets
        // ─────────────────────────────────────────────────────────────────────
        Map<Boolean, List<Integer>> evenOdd = boxed.stream()
                .collect(Collectors.partitioningBy(x -> x % 2 == 0));

        // ─────────────────────────────────────────────────────────────────────
        // 9. joining — fast comma-separated output
        // ─────────────────────────────────────────────────────────────────────
        String csv = words.stream().collect(Collectors.joining(","));
        String wrapped = words.stream().collect(Collectors.joining(", ", "[", "]"));

        // ─────────────────────────────────────────────────────────────────────
        // 10. toMap with merge — when keys can collide
        // ─────────────────────────────────────────────────────────────────────
        Map<Character, Integer> charSum = words.stream()
                .collect(Collectors.toMap(
                        w -> w.charAt(0),
                        String::length,
                        Integer::sum));                       // merger for duplicate keys

        // ─────────────────────────────────────────────────────────────────────
        // 11. reduce — fold the stream to a single value
        // ─────────────────────────────────────────────────────────────────────
        int product = IntStream.rangeClosed(1, 5).reduce(1, (a, b) -> a * b);   // 120

        // ─────────────────────────────────────────────────────────────────────
        // 12. Output
        // ─────────────────────────────────────────────────────────────────────
        System.out.printf("sum=%d max=%d min=%d avg=%.2f cnt=%d%n", sum, max, min, avg, cnt);
        System.out.println("sorted   = " + Arrays.toString(sorted));
        System.out.println("distinct = " + Arrays.toString(distinct));
        System.out.println("squares  = " + Arrays.toString(squares));
        System.out.println("evens    = " + Arrays.toString(evens));
        System.out.println("freq     = " + freq);
        System.out.println("groups   = " + groups);
        System.out.println("evenOdd  = " + evenOdd);
        System.out.println("csv      = " + csv);
        System.out.println("wrapped  = " + wrapped);
        System.out.println("charSum  = " + charSum);
        System.out.println("product  = " + product);
        System.out.printf("anyOver5=%b allPos=%b noneNeg=%b  primLen=%d%n",
                anyOver5, allPos, noneNeg, prim.length);

        // ─────────────────────────────────────────────────────────────────────
        // When NOT to use streams:
        //   • You need to break / return early — loops are clearer (and faster).
        //   • You're inside a hot inner loop in a tight time limit.
        //   • You need to throw checked exceptions from the lambda — annoying.
        //   • You want to mutate external state — use a loop.
        // ─────────────────────────────────────────────────────────────────────
    }
}