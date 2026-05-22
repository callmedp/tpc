package dsa;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  MODULE 27 — STOCK PROBLEMS DP                                            │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 *   The "Best Time to Buy and Sell Stock" series teaches you to model
 *   STATE MACHINES with DP. Same shape every time:
 *
 *      hold[i]  = max profit at end of day i when HOLDING a share
 *      cash[i]  = max profit at end of day i when NOT holding a share
 *
 *   Plus extra dimensions for:
 *      • k = number of transactions used (LC 188 / 123)
 *      • cooldown      (LC 309 — add a "just sold yesterday" state)
 *      • transaction fee (LC 714)
 *
 *   All solutions become 6-line state-machine loops.
 *
 * Worked problems in this file:
 *   1. LC 121  Best Time I — at most ONE transaction
 *   2. LC 122  Best Time II — unlimited transactions
 *   3. LC 123  Best Time III — at most TWO transactions
 *   4. LC 188  Best Time IV — at most K transactions
 *   5. LC 309  With Cooldown
 *   6. LC 714  With Transaction Fee
 */
public class Module27_DPStocks {

    // 1. LC 121 — at most one transaction; track running min price.
    static int maxProfitI(int[] p) {
        int min = Integer.MAX_VALUE, best = 0;
        for (int x : p) {
            if (x < min) min = x;
            else best = Math.max(best, x - min);
        }
        return best;
    }

    // 2. LC 122 — unlimited transactions; sum of all positive deltas.
    static int maxProfitII(int[] p) {
        int profit = 0;
        for (int i = 1; i < p.length; i++) if (p[i] > p[i - 1]) profit += p[i] - p[i - 1];
        return profit;
    }

    // 3. LC 123 — at most two transactions; four-state DP (or state-machine).
    //    buy1 = -p[i] best after first buy
    //    sell1 = max(sell1, buy1 + p[i])
    //    buy2 = max(buy2, sell1 - p[i])
    //    sell2 = max(sell2, buy2 + p[i])
    static int maxProfitIII(int[] p) {
        int buy1 = Integer.MIN_VALUE, sell1 = 0;
        int buy2 = Integer.MIN_VALUE, sell2 = 0;
        for (int x : p) {
            buy1  = Math.max(buy1, -x);
            sell1 = Math.max(sell1, buy1 + x);
            buy2  = Math.max(buy2, sell1 - x);
            sell2 = Math.max(sell2, buy2 + x);
        }
        return sell2;
    }

    // 4. LC 188 — at most K transactions; generalise III with arrays of size K.
    //    Edge case: when K ≥ n/2, problem degenerates to LC 122 (unlimited).
    static int maxProfitIV(int K, int[] p) {
        int n = p.length;
        if (K >= n / 2) return maxProfitII(p);
        int[] buy = new int[K + 1], sell = new int[K + 1];
        java.util.Arrays.fill(buy, Integer.MIN_VALUE);
        for (int x : p)
            for (int k = 1; k <= K; k++) {
                buy[k]  = Math.max(buy[k], sell[k - 1] - x);
                sell[k] = Math.max(sell[k], buy[k] + x);
            }
        return sell[K];
    }

    // 5. LC 309 — With Cooldown (one-day cooldown after a sell)
    //    State machine:  hold, sold (just sold today), rest (idle).
    //    Transitions:
    //       hold[i] = max(hold[i-1], rest[i-1] - p[i])
    //       sold[i] = hold[i-1] + p[i]
    //       rest[i] = max(rest[i-1], sold[i-1])
    static int maxProfitCooldown(int[] p) {
        int hold = Integer.MIN_VALUE, sold = 0, rest = 0;
        for (int x : p) {
            int prevSold = sold;
            sold = hold + x;
            hold = Math.max(hold, rest - x);
            rest = Math.max(rest, prevSold);
        }
        return Math.max(sold, rest);
    }

    // 6. LC 714 — With Transaction Fee
    //    cash = max profit not holding; hold = max profit holding.
    //    cash = max(cash, hold + p - fee)
    //    hold = max(hold, cash - p)
    static int maxProfitFee(int[] p, int fee) {
        int cash = 0, hold = -p[0];
        for (int i = 1; i < p.length; i++) {
            cash = Math.max(cash, hold + p[i] - fee);
            hold = Math.max(hold, cash - p[i]);
        }
        return cash;
    }

    public static void main(String[] args) {
        int[] p = {7, 1, 5, 3, 6, 4};
        System.out.println("maxProfitI   (1 txn)   = " + maxProfitI(p));                              // 5
        System.out.println("maxProfitII  (∞ txn)   = " + maxProfitII(p));                              // 7
        System.out.println("maxProfitIII (2 txn)   = " + maxProfitIII(new int[]{3, 3, 5, 0, 0, 3, 1, 4})); // 6
        System.out.println("maxProfitIV  (k=2)     = " + maxProfitIV(2, new int[]{3, 2, 6, 5, 0, 3})); // 7
        System.out.println("maxProfitCooldown      = " + maxProfitCooldown(new int[]{1, 2, 3, 0, 2})); // 3
        System.out.println("maxProfitFee (fee=2)   = " + maxProfitFee(new int[]{1, 3, 2, 8, 4, 9}, 2)); // 8
    }

    /*
     * PRACTICE SET
     *   • LC 901 (related):   Online Stock Span (monotonic stack — Module 8)
     *   • LC 2291 Maximum Profit From Trading Stocks    (knapsack)
     *   • LC 2222 Number of Ways to Select Buildings    (linear DP)
     *   • LC 2110 Number of Smooth Descent Periods of Stock
     *   • Variant: "with at most one short sell allowed" → 3-state DP
     */
}
