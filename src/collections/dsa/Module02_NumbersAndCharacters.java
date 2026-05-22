package collections.dsa;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  MODULE 2 — NUMBERS & CHARACTERS                                         │
 * │  Prereq:  Module 1                                                       │
 * │  Goal:    Math, Integer, Long, Character — the scalar utility toolbox    │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * Every DSA problem involves arithmetic and char checks. The classes here
 * cover overflow-safe math, bit tricks, signed-vs-unsigned, and char tests.
 *
 * What you'll learn:
 *   • Math: max/min/abs, floor/ceil, pow/sqrt/log
 *   • Math: floorMod / floorDiv / ceilDiv — correct semantics for negatives
 *   • Math: addExact / subtractExact / multiplyExact — overflow detection
 *   • Integer: MAX_VALUE / MIN_VALUE — sentinel pattern
 *   • Integer: parseInt / toString(radix) / toBinaryString
 *   • Integer: bitCount, highestOneBit, lowestOneBit, leading/trailing zeros
 *   • Integer: compare, signum, reverseBits
 *   • Character: isDigit / isLetter / isLetterOrDigit / isWhitespace
 *   • Character: toLowerCase / toUpperCase / getNumericValue / digit
 */
public class Module02_NumbersAndCharacters {

    public static void main(String[] args) {

        // ─────────────────────────────────────────────────────────────────────
        // 1. Math — basic helpers
        // ─────────────────────────────────────────────────────────────────────
        Math.max(3, 7);  Math.min(3, 7);
        Math.abs(-5);
        Math.pow(2, 10);              // returns double — cast if needed
        Math.sqrt(2);   Math.log(2);  Math.log10(1000);
        Math.floor(2.7);  Math.ceil(2.3);  Math.round(2.5);

        // ─────────────────────────────────────────────────────────────────────
        // 2. Math — negatives, modulo, integer division   (common interview gotcha)
        //    Java's % keeps the sign of the dividend:
        //      -1 % 5  ==  -1
        //    The mathematical modulo is given by Math.floorMod:
        //      Math.floorMod(-1, 5) == 4
        // ─────────────────────────────────────────────────────────────────────
        int badMod  = -1 % 5;                  // -1
        int goodMod = Math.floorMod(-1, 5);    //  4

        int floorDiv = Math.floorDiv(-7, 2);                  // -4   (regular / gives -3)
        // Math.ceilDiv exists from Java 18+. Pre-18 idiom for non-negative inputs:
        int ceilDiv  = (7 + 2 - 1) / 2;                       //  4   ((a + b - 1) / b)

        // ─────────────────────────────────────────────────────────────────────
        // 3. Math — overflow-safe arithmetic
        // ─────────────────────────────────────────────────────────────────────
        try {
            Math.addExact(Integer.MAX_VALUE, 1);     // throws ArithmeticException
        } catch (ArithmeticException ignored) {}
        // Math.subtractExact, Math.multiplyExact, Math.negateExact, Math.toIntExact

        // ─────────────────────────────────────────────────────────────────────
        // 4. Integer — parsing, formatting, sentinels
        // ─────────────────────────────────────────────────────────────────────
        int hi = Integer.MAX_VALUE, lo = Integer.MIN_VALUE;   // sentinel pattern
        int parsed = Integer.parseInt("42");
        int hex    = Integer.parseInt("2a", 16);              // 42
        String dec = Integer.toString(42);
        String bin = Integer.toBinaryString(42);              // "101010"
        String oct = Integer.toString(42, 8);                 // "52"
        Integer.compare(3, 7);                                // -1, 0, 1   (safe vs a-b overflow)
        Integer.signum(-9);                                   // -1

        // ─────────────────────────────────────────────────────────────────────
        // 5. Integer — bit utilities  (LeetCode "Bit Manipulation" tag)
        // ─────────────────────────────────────────────────────────────────────
        int n = 11;                                           // binary 1011
        Integer.bitCount(n);                                  // 3   set bits ("Hamming weight")
        Integer.highestOneBit(n);                             // 8   keeps only the top set bit
        Integer.lowestOneBit(n);                              // 1   keeps only the bottom set bit
        Integer.numberOfLeadingZeros(n);                      // 28
        Integer.numberOfTrailingZeros(8);                     // 3
        Integer.reverse(n);                                   // bit-reverse a 32-bit value

        // ─────────────────────────────────────────────────────────────────────
        // 6. Hand-rolled bit tricks every interviewer expects you to know
        // ─────────────────────────────────────────────────────────────────────
        boolean isPow2     = n > 0 && (n & (n - 1)) == 0;     // power of two
        int turnOffLowest  = n & (n - 1);                     // clears lowest set bit
        int isolateLowest  = n & -n;                          // keeps only lowest set bit
        int toggleBit3     = n ^ (1 << 3);                    // flip bit at position 3
        boolean isBit2Set  = (n & (1 << 2)) != 0;             // test bit at position 2
        int setBit5        = n | (1 << 5);                    // set bit at position 5

        // ─────────────────────────────────────────────────────────────────────
        // 7. Long — same API, just bigger
        // ─────────────────────────────────────────────────────────────────────
        long lhi = Long.MAX_VALUE, llo = Long.MIN_VALUE;
        Long.bitCount(0xFFFFFFFFFFL);
        Long.parseLong("9999999999");

        // ─────────────────────────────────────────────────────────────────────
        // 8. Character — classify input chars in a single API
        // ─────────────────────────────────────────────────────────────────────
        char c = '7';
        Character.isDigit(c);              // '0'..'9'
        Character.isLetter('A');
        Character.isLetterOrDigit('A');    // alphanumeric — LC "Valid Palindrome" filter
        Character.isUpperCase('A');
        Character.isLowerCase('a');
        Character.isWhitespace(' ');
        Character.isAlphabetic('A');       // covers Unicode letters
        Character.toLowerCase('A');
        Character.toUpperCase('a');
        Character.getNumericValue('7');    // 7   (also 'A' -> 10, 'F' -> 15 for radix)
        Character.digit('7', 10);          // 7   (or -1 if not a digit in that radix)

        // Fast manual char→int for ASCII digits — preferred in hot loops
        int d = '7' - '0';

        // ─────────────────────────────────────────────────────────────────────
        // 9. Showcase prints
        // ─────────────────────────────────────────────────────────────────────
        System.out.printf("badMod=%d  goodMod=%d  floorDiv=%d  ceilDiv=%d%n",
                badMod, goodMod, floorDiv, ceilDiv);
        System.out.printf("parsed=%d  hex=%d  dec=%s  bin=%s  oct=%s%n",
                parsed, hex, dec, bin, oct);
        System.out.printf("bitCount(11)=%d  isPow2(12)=%b  isolateLowest(12)=%d%n",
                Integer.bitCount(11), 12 > 0 && (12 & 11) == 0, 12 & -12);
        System.out.printf("isPow2(%d)=%b  turnOffLowest=%d  toggle=%d  bit2Set=%b%n",
                n, isPow2, turnOffLowest, toggleBit3, isBit2Set);
        System.out.printf("digit('7')=%d  hi=%d  lo=%d  setBit5=%d  d=%d%n",
                Character.getNumericValue('7'), hi, lo, setBit5, d);

        // ─────────────────────────────────────────────────────────────────────
        // Practice problems:
        //   • LC 191  Number of 1 Bits           — Integer.bitCount or n & (n-1)
        //   • LC 231  Power of Two               — (n & (n-1)) == 0
        //   • LC 268  Missing Number (XOR)
        //   • LC 371  Sum of Two Integers (no +) — bit shifting
        //   • LC 7    Reverse Integer            — Math overflow check
        // ─────────────────────────────────────────────────────────────────────
    }
}