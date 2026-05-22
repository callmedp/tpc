package dsa;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  MODULE 23 — DP ON STRINGS (LCS / LIS / EDIT DISTANCE / WILDCARD)        │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 *   String DP almost always has the shape:
 *      dp[i][j] = "best/count for s1[0..i-1] vs s2[0..j-1]"
 *   Recurrence cases by whether s1[i-1] matches s2[j-1].
 *
 * BIG IDEAS
 *   • LCS — match (+1) or skip one side.
 *   • Edit Distance — match, or one of {insert, delete, replace}.
 *   • Palindromic DP — dp[i][j] = "is s[i..j] palindrome?" or "longest palindromic subseq".
 *   • Regex / wildcard — break by current pattern char.
 *
 * Worked problems in this file:
 *   1. LC 1143  Longest Common Subsequence (LCS)
 *   2. LC 72    Edit Distance
 *   3. LC 583   Delete Operations to Make Equal
 *   4. LC 5     Longest Palindromic Substring
 *   5. LC 516   Longest Palindromic Subsequence
 *   6. LC 647   Palindromic Substrings (count)
 *   7. LC 10    Regular Expression Matching
 *   8. LC 44    Wildcard Matching
 *   9. LC 139   Word Break
 */
public class Module23_DPStrings {

    // 1. LC 1143 — Longest Common Subsequence
    static int longestCommonSubsequence(String a, String b) {
        int n = a.length(), m = b.length();
        int[][] dp = new int[n + 1][m + 1];
        for (int i = 1; i <= n; i++)
            for (int j = 1; j <= m; j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) dp[i][j] = dp[i - 1][j - 1] + 1;
                else                                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
            }
        return dp[n][m];
    }

    // 2. LC 72 — Edit Distance
    //    Match: dp[i-1][j-1].   Mismatch: 1 + min(dp[i-1][j], dp[i][j-1], dp[i-1][j-1]).
    static int minDistance(String a, String b) {
        int n = a.length(), m = b.length();
        int[][] dp = new int[n + 1][m + 1];
        for (int i = 0; i <= n; i++) dp[i][0] = i;
        for (int j = 0; j <= m; j++) dp[0][j] = j;
        for (int i = 1; i <= n; i++)
            for (int j = 1; j <= m; j++)
                if (a.charAt(i - 1) == b.charAt(j - 1)) dp[i][j] = dp[i - 1][j - 1];
                else dp[i][j] = 1 + Math.min(dp[i - 1][j - 1], Math.min(dp[i - 1][j], dp[i][j - 1]));
        return dp[n][m];
    }

    // 3. LC 583 — Delete Operations to Make Equal
    //    Answer = (n + m - 2 · LCS).
    static int minDeletionsToEqual(String a, String b) {
        return a.length() + b.length() - 2 * longestCommonSubsequence(a, b);
    }

    // 4. LC 5 — Longest Palindromic Substring (expand around centre, O(n²) time, O(1) space)
    static String longestPalindrome(String s) {
        int bestL = 0, bestR = 0;
        for (int i = 0; i < s.length(); i++) {
            int[] a = expand(s, i, i);                 // odd length
            int[] b = expand(s, i, i + 1);             // even length
            if (a[1] - a[0] > bestR - bestL) { bestL = a[0]; bestR = a[1]; }
            if (b[1] - b[0] > bestR - bestL) { bestL = b[0]; bestR = b[1]; }
        }
        return s.substring(bestL, bestR + 1);
    }
    private static int[] expand(String s, int l, int r) {
        while (l >= 0 && r < s.length() && s.charAt(l) == s.charAt(r)) { l--; r++; }
        return new int[]{l + 1, r - 1};
    }

    // 5. LC 516 — Longest Palindromic Subsequence
    //    dp[i][j] = LPS in s[i..j].  Fill by length.
    static int longestPalindromeSubseq(String s) {
        int n = s.length();
        int[][] dp = new int[n][n];
        for (int i = 0; i < n; i++) dp[i][i] = 1;
        for (int len = 2; len <= n; len++)
            for (int i = 0; i + len <= n; i++) {
                int j = i + len - 1;
                if (s.charAt(i) == s.charAt(j))
                    dp[i][j] = (len == 2 ? 2 : dp[i + 1][j - 1] + 2);
                else
                    dp[i][j] = Math.max(dp[i + 1][j], dp[i][j - 1]);
            }
        return dp[0][n - 1];
    }

    // 6. LC 647 — Palindromic Substrings (count)
    static int countSubstrings(String s) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            count += countPal(s, i, i);
            count += countPal(s, i, i + 1);
        }
        return count;
    }
    private static int countPal(String s, int l, int r) {
        int c = 0;
        while (l >= 0 && r < s.length() && s.charAt(l) == s.charAt(r)) { c++; l--; r++; }
        return c;
    }

    // 7. LC 10 — Regular Expression Matching ('.', '*')
    //    dp[i][j] = does p[0..j-1] match s[0..i-1]?
    static boolean isMatchRegex(String s, String p) {
        int n = s.length(), m = p.length();
        boolean[][] dp = new boolean[n + 1][m + 1];
        dp[0][0] = true;
        for (int j = 1; j <= m; j++)
            if (p.charAt(j - 1) == '*') dp[0][j] = dp[0][j - 2];
        for (int i = 1; i <= n; i++)
            for (int j = 1; j <= m; j++) {
                if (p.charAt(j - 1) == '.' || p.charAt(j - 1) == s.charAt(i - 1)) dp[i][j] = dp[i - 1][j - 1];
                else if (p.charAt(j - 1) == '*') {
                    dp[i][j] = dp[i][j - 2];
                    if (p.charAt(j - 2) == '.' || p.charAt(j - 2) == s.charAt(i - 1)) dp[i][j] |= dp[i - 1][j];
                }
            }
        return dp[n][m];
    }

    // 8. LC 44 — Wildcard Matching ('?', '*' meaning ANY sequence)
    static boolean isMatchWildcard(String s, String p) {
        int n = s.length(), m = p.length();
        boolean[][] dp = new boolean[n + 1][m + 1];
        dp[0][0] = true;
        for (int j = 1; j <= m; j++)
            if (p.charAt(j - 1) == '*') dp[0][j] = dp[0][j - 1];
        for (int i = 1; i <= n; i++)
            for (int j = 1; j <= m; j++) {
                char pc = p.charAt(j - 1);
                if (pc == '?' || pc == s.charAt(i - 1)) dp[i][j] = dp[i - 1][j - 1];
                else if (pc == '*')                     dp[i][j] = dp[i - 1][j] || dp[i][j - 1];
            }
        return dp[n][m];
    }

    // 9. LC 139 — Word Break
    //    dp[i] = can s[0..i-1] be segmented?  Check every suffix word.
    static boolean wordBreak(String s, java.util.List<String> dict) {
        java.util.Set<String> set = new java.util.HashSet<>(dict);
        int n = s.length();
        boolean[] dp = new boolean[n + 1];
        dp[0] = true;
        for (int i = 1; i <= n; i++)
            for (int j = 0; j < i; j++)
                if (dp[j] && set.contains(s.substring(j, i))) { dp[i] = true; break; }
        return dp[n];
    }

    public static void main(String[] args) {
        System.out.println("LCS abcde / ace            = " + longestCommonSubsequence("abcde", "ace"));        // 3
        System.out.println("editDistance horse→ros     = " + minDistance("horse", "ros"));                      // 3
        System.out.println("minDeletionsToEqual        = " + minDeletionsToEqual("sea", "eat"));                 // 2
        System.out.println("longestPalindrome(babad)   = " + longestPalindrome("babad"));                       // bab or aba
        System.out.println("longestPalindromeSubseq    = " + longestPalindromeSubseq("bbbab"));                  // 4
        System.out.println("countSubstrings(aaa)       = " + countSubstrings("aaa"));                            // 6
        System.out.println("isMatchRegex aab,c*a*b     = " + isMatchRegex("aab", "c*a*b"));                       // true
        System.out.println("isMatchWildcard adceb,*a*b = " + isMatchWildcard("adceb", "*a*b"));                   // true
        System.out.println("wordBreak leetcode         = " + wordBreak("leetcode", java.util.List.of("leet", "code"))); // true
    }

    /*
     * PRACTICE SET
     *   • LC 132   Palindrome Partitioning II
     *   • LC 91    Decode Ways (linear DP — Module 20)
     *   • LC 97    Interleaving String
     *   • LC 115   Distinct Subsequences
     *   • LC 121   Best Time to Buy and Sell Stock     (covered in Module 27)
     *   • LC 161   One Edit Distance
     *   • LC 392   Is Subsequence
     *   • LC 416   Partition Equal Subset Sum         (knapsack — Module 22)
     *   • LC 727   Min Window Subsequence
     *   • LC 1312  Minimum Insertions to Palindrome
     */
}
