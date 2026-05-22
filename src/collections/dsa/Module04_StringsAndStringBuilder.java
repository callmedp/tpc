package collections.dsa;

import java.util.stream.Collectors;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  MODULE 4 — STRINGS & STRINGBUILDER                                      │
 * │  Prereq:  Modules 1–2                                                    │
 * │  Goal:    every String / StringBuilder method used in interviews         │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * String problems are the single most common interview topic. Get fluent
 * with these and you'll move twice as fast on palindromes, parsing,
 * anagrams, sliding-window-on-string, etc.
 *
 * Cardinal rule: STRINGS ARE IMMUTABLE.
 *   - "abc" + "d" allocates a new String. In a loop that becomes O(n²) garbage.
 *   - Always build with StringBuilder.
 *
 * What you'll learn:
 *   • String   — read, slice, search, compare, transform
 *   • String   — split, replace, repeat, format, join, chars(), codePoints()
 *   • String   — toCharArray (the bridge into char[] DSA work)
 *   • StringBuilder — append/insert/delete/reverse/replace/setCharAt
 *   • Palindrome / anagram helpers
 */
public class Module04_StringsAndStringBuilder {

    public static void main(String[] args) {

        // ─────────────────────────────────────────────────────────────────────
        // 1. Reading
        // ─────────────────────────────────────────────────────────────────────
        String s = "interview";
        s.length();                          // 9
        s.charAt(0);                         // 'i'
        s.isEmpty();                         // false
        s.isBlank();                         // false   (Java 11)  — whitespace-only check
        s.toCharArray();                     // ['i','n',…]  — bridge to char[] DSA work

        // ─────────────────────────────────────────────────────────────────────
        // 2. Slicing
        // ─────────────────────────────────────────────────────────────────────
        s.substring(2);                      // "terview"
        s.substring(2, 5);                   // "ter"     — [from, to)

        // ─────────────────────────────────────────────────────────────────────
        // 3. Searching
        // ─────────────────────────────────────────────────────────────────────
        s.indexOf('v');                      // 4
        s.indexOf("view");                   // 5
        s.indexOf('e', 3);                   // start search from index 3
        s.lastIndexOf('e');
        s.contains("view");
        s.startsWith("inter");
        s.endsWith("view");

        // ─────────────────────────────────────────────────────────────────────
        // 4. Comparing
        // ─────────────────────────────────────────────────────────────────────
        s.equals("INTERVIEW");                 // false
        s.equalsIgnoreCase("INTERVIEW");       // true
        s.compareTo("apple");                  // lex order — useful in custom comparators
        s.compareToIgnoreCase("INTERVIEW");

        // ─────────────────────────────────────────────────────────────────────
        // 5. Transforming (each call creates a NEW String)
        // ─────────────────────────────────────────────────────────────────────
        s.toLowerCase();   s.toUpperCase();
        "  pad  ".trim();                      // strips ASCII whitespace
        "  pad  ".strip();                     // strips Unicode whitespace (Java 11)
        "  pad  ".stripLeading();              // left-only
        "  pad  ".stripTrailing();             // right-only
        s.replace('v', 'V');                   // ALL occurrences (char or String)
        s.replace("inter", "OUTER");
        s.replaceFirst("e", "3");              // regex!  escape \. \+ etc.
        s.replaceAll("[aeiou]", "_");          // regex
        "abc".repeat(3);                       // "abcabcabc" (Java 11)
        String.format("score=%03d  name=%-8s", 7, "amy");
        String.join(",", "a", "b", "c");       // "a,b,c"
        String.valueOf(42);                    // "42" — works on any type

        // ─────────────────────────────────────────────────────────────────────
        // 6. Splitting
        //    s.split(regex) — pattern is a REGEX, escape literals.
        // ─────────────────────────────────────────────────────────────────────
        "1,2,3,4".split(",");                  // ["1","2","3","4"]
        "a.b.c".split("\\.");                  // dots must be escaped
        "1 2   3".split("\\s+");               // any whitespace run
        "a,b,,c".split(",");                   // ["a","b","","c"]
        "a,b,,c".split(",", -1);               // limit=-1 keeps trailing empties

        // ─────────────────────────────────────────────────────────────────────
        // 7. Streams over chars  (useful one-liners)
        // ─────────────────────────────────────────────────────────────────────
        long vowelCount = "interview".chars()
                .filter(c -> "aeiou".indexOf(c) >= 0)
                .count();

        String sorted = "banana".chars()
                .sorted()
                .collect(StringBuilder::new,
                         (sb, c) -> sb.append((char) c),
                         StringBuilder::append)
                .toString();                   // "aaabnn" — anagram canonical key

        // ─────────────────────────────────────────────────────────────────────
        // 8. StringBuilder — the mutable buddy.  ALWAYS use this in loops.
        // ─────────────────────────────────────────────────────────────────────
        StringBuilder sb = new StringBuilder();
        sb.append("a").append(1).append(true);   // overloaded for every primitive
        sb.insert(0, "X");                       // insert at index 0
        sb.deleteCharAt(0);                      // remove index 0
        sb.delete(1, 3);                         // remove range [1, 3)
        sb.replace(0, 1, "Z");                   // replace range with string
        sb.setCharAt(0, 'Q');                    // overwrite one char
        sb.charAt(0);
        sb.length();
        sb.setLength(2);                         // TRUNCATE or pad with '\0'
        sb.reverse();                            // in-place — useful for palindrome / Atoi
        String built = sb.toString();

        // ─────────────────────────────────────────────────────────────────────
        // 9. Palindrome check (clean idiom)
        // ─────────────────────────────────────────────────────────────────────
        String p = "racecar";
        boolean pal = p.contentEquals(new StringBuilder(p).reverse());

        // Two-pointer version (faster — no allocation)
        boolean pal2 = isPalindrome(p);

        // ─────────────────────────────────────────────────────────────────────
        // 10. Anagram check (canonical-key idiom)
        // ─────────────────────────────────────────────────────────────────────
        String a = "listen", b = "silent";
        boolean anagram = sortChars(a).equals(sortChars(b));

        // ─────────────────────────────────────────────────────────────────────
        System.out.println("built       = " + built);
        System.out.println("palindrome  = " + pal + "  pal2=" + pal2);
        System.out.println("anagram     = " + anagram);
        System.out.println("vowels(int) = " + vowelCount);
        System.out.println("sorted      = " + sorted);

        // ─────────────────────────────────────────────────────────────────────
        // Practice problems:
        //   • LC 125  Valid Palindrome           — Character.isLetterOrDigit + two pointers
        //   • LC 242  Valid Anagram              — sort chars OR int[26] counts
        //   • LC 49   Group Anagrams             — sorted-key + HashMap (Module 5)
        //   • LC 5    Longest Palindromic Substr — expand-around-center
        //   • LC 8    String to Integer (atoi)   — careful overflow with Math.addExact
        //   • LC 14   Longest Common Prefix      — char-by-char compare
        //   • LC 28   strStr / indexOf
        //   • LC 387  First Unique Character     — int[26] counts, two passes
        // ─────────────────────────────────────────────────────────────────────
    }

    private static String sortChars(String s) {
        char[] c = s.toCharArray();
        java.util.Arrays.sort(c);
        return new String(c);
    }

    private static boolean isPalindrome(String s) {
        int i = 0, j = s.length() - 1;
        while (i < j) {
            if (s.charAt(i++) != s.charAt(j--)) return false;
        }
        return true;
    }
}