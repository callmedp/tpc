package dsa;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  MODULE 13 — TRIE (PREFIX TREE)                                          │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * WHEN TO RECOGNIZE
 *   • Many words to search for a PREFIX (autocomplete, dictionary).
 *   • You need O(len) prefix lookup, not O(N · len) over a HashSet.
 *   • "Word Search II" — multi-word board search, prune branches that no
 *     dictionary word can extend.
 *   • Bitwise tricks (XOR maximum) on a binary trie.
 *
 * THE STRUCTURE — one node per character. Children are usually a
 * fixed-size array (TrieNode[26]) for lower-case English, or a HashMap
 * for arbitrary alphabets.
 *
 *      class TrieNode {
 *          TrieNode[] kids = new TrieNode[26];
 *          boolean    end;
 *      }
 *
 * COMPLEXITY  — insert / search / startsWith  = O(L) where L = key length.
 *               Space = O(total chars).
 *
 * Worked problems in this file:
 *   1. LC 208  Implement Trie (Prefix Tree)
 *   2. LC 211  Add and Search Word — Data Structure ('.' wildcard)
 *   3. LC 648  Replace Words                       (shortest root)
 *   4. LC 677  Map Sum Pairs                       (prefix sum on trie)
 *   5. LC 212  Word Search II  (board + dictionary, DFS pruned by trie)
 *   6. LC 421  Max XOR of Two Numbers (binary trie)
 */
public class Module13_Trie {

    // ─────────────────────────────────────────────────────────────────────────
    // 1. LC 208 — Implement Trie
    // ─────────────────────────────────────────────────────────────────────────
    static class Trie {
        TrieNode root = new TrieNode();

        void insert(String w) {
            TrieNode n = root;
            for (char c : w.toCharArray()) {
                int i = c - 'a';
                if (n.kids[i] == null) n.kids[i] = new TrieNode();
                n = n.kids[i];
            }
            n.end = true;
        }
        boolean search(String w)      { TrieNode n = walk(w); return n != null && n.end; }
        boolean startsWith(String pf) { return walk(pf) != null; }
        private TrieNode walk(String w) {
            TrieNode n = root;
            for (char c : w.toCharArray()) {
                n = n.kids[c - 'a'];
                if (n == null) return null;
            }
            return n;
        }
    }

    static class TrieNode {
        TrieNode[] kids = new TrieNode[26];
        boolean end;
        String  word;        // optional — convenient for LC 212
        int     sum;         // optional — convenient for LC 677
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. LC 211 — WordDictionary  (search supports '.' wildcard)
    //    DFS over the trie, branching on '.'
    // ─────────────────────────────────────────────────────────────────────────
    static class WordDictionary {
        TrieNode root = new TrieNode();
        void addWord(String w) {
            TrieNode n = root;
            for (char c : w.toCharArray()) {
                if (n.kids[c - 'a'] == null) n.kids[c - 'a'] = new TrieNode();
                n = n.kids[c - 'a'];
            }
            n.end = true;
        }
        boolean search(String w) { return dfs(root, w, 0); }
        private boolean dfs(TrieNode n, String w, int i) {
            if (n == null) return false;
            if (i == w.length()) return n.end;
            char c = w.charAt(i);
            if (c == '.') {
                for (TrieNode k : n.kids) if (dfs(k, w, i + 1)) return true;
                return false;
            }
            return dfs(n.kids[c - 'a'], w, i + 1);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. LC 648 — Replace Words (shortest matching root)
    // ─────────────────────────────────────────────────────────────────────────
    static String replaceWords(List<String> roots, String sentence) {
        Trie tr = new Trie();
        for (String r : roots) tr.insert(r);
        StringBuilder out = new StringBuilder();
        for (String w : sentence.split(" ")) {
            if (out.length() > 0) out.append(' ');
            TrieNode n = tr.root;
            int i = 0;
            for (; i < w.length(); i++) {
                int idx = w.charAt(i) - 'a';
                if (n.kids[idx] == null) break;
                n = n.kids[idx];
                if (n.end) { i++; break; }
            }
            out.append(n.end ? w.substring(0, i) : w);
        }
        return out.toString();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. LC 677 — Map Sum Pairs (insert (key,val); sum over a prefix)
    //    Track delta in each insert so we can update without re-walking the old chain.
    // ─────────────────────────────────────────────────────────────────────────
    static class MapSum {
        TrieNode root = new TrieNode();
        java.util.Map<String, Integer> prev = new java.util.HashMap<>();
        void insert(String key, int val) {
            int delta = val - prev.getOrDefault(key, 0);
            prev.put(key, val);
            TrieNode n = root;
            for (char c : key.toCharArray()) {
                if (n.kids[c - 'a'] == null) n.kids[c - 'a'] = new TrieNode();
                n = n.kids[c - 'a'];
                n.sum += delta;
            }
        }
        int sum(String pf) {
            TrieNode n = root;
            for (char c : pf.toCharArray()) {
                n = n.kids[c - 'a'];
                if (n == null) return 0;
            }
            return n.sum;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. LC 212 — Word Search II
    //    Insert every dictionary word into a trie; DFS the board, walking the
    //    trie in lockstep. Prune as soon as the prefix isn't in the trie.
    // ─────────────────────────────────────────────────────────────────────────
    static List<String> findWords(char[][] board, String[] words) {
        Trie tr = new Trie();
        for (String w : words) {
            TrieNode n = tr.root;
            for (char c : w.toCharArray()) {
                if (n.kids[c - 'a'] == null) n.kids[c - 'a'] = new TrieNode();
                n = n.kids[c - 'a'];
            }
            n.end = true; n.word = w;
        }
        Set<String> out = new HashSet<>();
        for (int r = 0; r < board.length; r++)
            for (int c = 0; c < board[0].length; c++)
                dfsBoard(board, r, c, tr.root, out);
        return new ArrayList<>(out);
    }
    private static void dfsBoard(char[][] b, int r, int c, TrieNode n, Set<String> out) {
        if (r < 0 || c < 0 || r >= b.length || c >= b[0].length) return;
        char ch = b[r][c];
        if (ch == '#' || n.kids[ch - 'a'] == null) return;
        n = n.kids[ch - 'a'];
        if (n.end) out.add(n.word);
        b[r][c] = '#';
        dfsBoard(b, r + 1, c, n, out);
        dfsBoard(b, r - 1, c, n, out);
        dfsBoard(b, r, c + 1, n, out);
        dfsBoard(b, r, c - 1, n, out);
        b[r][c] = ch;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. LC 421 — Maximum XOR of Two Numbers (binary trie)
    //    Insert each number bit-by-bit (MSB first). For each x, greedily walk
    //    the trie taking the opposite bit when possible to maximise XOR.
    // ─────────────────────────────────────────────────────────────────────────
    static int findMaximumXOR(int[] nums) {
        BitTrie tr = new BitTrie();
        for (int n : nums) tr.insert(n);
        int best = 0;
        for (int n : nums) best = Math.max(best, tr.maxXor(n));
        return best;
    }
    static class BitTrie {
        BitTrie[] c = new BitTrie[2];
        void insert(int n) {
            BitTrie t = this;
            for (int i = 31; i >= 0; i--) {
                int b = (n >> i) & 1;
                if (t.c[b] == null) t.c[b] = new BitTrie();
                t = t.c[b];
            }
        }
        int maxXor(int n) {
            BitTrie t = this; int x = 0;
            for (int i = 31; i >= 0; i--) {
                int b = (n >> i) & 1;
                int want = b ^ 1;
                if (t.c[want] != null) { x |= (1 << i); t = t.c[want]; }
                else                   { t = t.c[b]; }
            }
            return x;
        }
    }

    public static void main(String[] args) {
        Trie tr = new Trie();
        tr.insert("apple");
        System.out.println("search(apple)    = " + tr.search("apple"));
        System.out.println("search(app)      = " + tr.search("app"));
        System.out.println("startsWith(app)  = " + tr.startsWith("app"));
        tr.insert("app");
        System.out.println("search(app) post = " + tr.search("app"));

        WordDictionary wd = new WordDictionary();
        wd.addWord("bad"); wd.addWord("dad"); wd.addWord("mad");
        System.out.println("WD search(.ad)   = " + wd.search(".ad"));
        System.out.println("WD search(b..)   = " + wd.search("b.."));

        System.out.println("replaceWords     = " + replaceWords(
                List.of("cat", "bat", "rat"),
                "the cattle was rattled by the battery"));

        MapSum ms = new MapSum();
        ms.insert("apple", 3);
        System.out.println("MapSum sum(ap)   = " + ms.sum("ap"));
        ms.insert("app", 2);
        System.out.println("MapSum sum(ap)   = " + ms.sum("ap"));

        char[][] board = {{'o','a','a','n'},{'e','t','a','e'},{'i','h','k','r'},{'i','f','l','v'}};
        System.out.println("findWords        = " + findWords(board, new String[]{"oath", "pea", "eat", "rain"}));

        System.out.println("findMaximumXOR   = " + findMaximumXOR(new int[]{3, 10, 5, 25, 2, 8}));
    }

    /*
     * PRACTICE SET
     *   • LC 14    Longest Common Prefix              (sort + compare, trie also works)
     *   • LC 1268  Search Suggestions System
     *   • LC 720   Longest Word in Dictionary
     *   • LC 745   Prefix and Suffix Search
     *   • LC 642   Design Search Autocomplete System
     *   • LC 1858  Longest Word With All Prefixes
     *   • LC 1804  Implement Trie II (counts)
     */
}
