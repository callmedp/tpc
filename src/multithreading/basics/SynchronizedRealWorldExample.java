package multithreading.basics;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SynchronizedRealWorldExample {

    // ================================================================
    // INSTANCE-LEVEL synchronized — BankAccount
    //
    // Each account has its OWN lock (this).
    // Two different accounts can be debited/credited in parallel.
    // But the same account cannot be touched by two threads at once.
    // ================================================================
    static class BankAccount {
        private final String owner;
        private double balance;

        BankAccount(String owner, double initialBalance) {
            this.owner = owner;
            this.balance = initialBalance;
        }

        // Lock = this account object — other accounts are unaffected
        synchronized void deposit(double amount) {
            balance += amount;
            System.out.printf("[%s] Deposited %.0f  → balance: %.0f%n", owner, amount, balance);
        }

        synchronized void withdraw(double amount) {
            if (balance < amount) {
                System.out.printf("[%s] Insufficient funds for withdrawal of %.0f%n", owner, amount);
                return;
            }
            balance -= amount;
            System.out.printf("[%s] Withdrew  %.0f  → balance: %.0f%n", owner, amount, balance);
        }

        synchronized double getBalance() { return balance; }
        String getOwner() { return owner; }
    }

    // ================================================================
    // CLASS-LEVEL synchronized — OrderIdGenerator
    //
    // The counter is static — shared across ALL instances.
    // static synchronized locks on OrderIdGenerator.class, not `this`.
    // Without this, two threads could get the same ID.
    // ================================================================
    static class OrderIdGenerator {
        private static int nextId = 1000;

        // Lock = OrderIdGenerator.class — one thread at a time, globally
        static synchronized int generateId() {
            int id = nextId;
            nextId++;
            return id;
        }
    }

    // ================================================================
    // DEMO: show both in action
    // ================================================================
    public static void run() throws InterruptedException {

        // --- Instance sync: Alice and Bob transact in parallel ---
        System.out.println("=== Instance-level synchronized (BankAccount) ===");

        BankAccount alice = new BankAccount("Alice", 1000);
        BankAccount bob   = new BankAccount("Bob",   500);

        ExecutorService pool = Executors.newFixedThreadPool(4);

        // Deposits and withdrawals on ALICE — serialised per account
        pool.execute(() -> alice.deposit(200));
        pool.execute(() -> alice.withdraw(150));
        pool.execute(() -> alice.deposit(300));

        // Deposits on BOB — run concurrently with Alice's transactions
        pool.execute(() -> bob.deposit(100));
        pool.execute(() -> bob.withdraw(200));

        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);

        System.out.printf("Final — %s: %.0f | %s: %.0f%n%n",
            alice.getOwner(), alice.getBalance(),
            bob.getOwner(),   bob.getBalance());

        // --- Class-level sync: 5 threads all generating order IDs ---
        System.out.println("=== Class-level synchronized (OrderIdGenerator) ===");

        ExecutorService orderPool = Executors.newFixedThreadPool(5);

        for (int i = 0; i < 10; i++) {
            orderPool.execute(() -> {
                int id = OrderIdGenerator.generateId();
                System.out.println(Thread.currentThread().getName() + " → Order ID: " + id);
            });
        }

        orderPool.shutdown();
        orderPool.awaitTermination(5, TimeUnit.SECONDS);

        System.out.println("\nAll IDs are unique — no duplicates possible with static synchronized.");
    }
}
