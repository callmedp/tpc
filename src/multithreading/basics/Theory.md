# Multithreading — Basics: Theory

---

## Q1. What is Multithreading?

**Multithreading** is the ability of a program to execute multiple threads concurrently within a single process.

- **Process** — an independent program running in its own memory space
- **Thread** — a lightweight unit of execution within a process; threads share the same memory space
- **Concurrency** — multiple threads making progress (possibly by interleaving on one CPU core)
- **Parallelism** — multiple threads literally executing at the same instant (requires multiple CPU cores)

### Why use it?
- **Performance** — split CPU-heavy work across cores
- **Responsiveness** — keep a UI responsive while doing background work
- **I/O efficiency** — while one thread waits on a network call, another thread can run

### Common Pitfalls

| Problem | Description |
|---|---|
| Race condition | Two threads read/write shared data simultaneously, producing unpredictable results |
| Deadlock | Two threads each wait for a lock the other holds — both freeze forever |
| Starvation | A thread never gets CPU time because others always run first |

---

## Q2. What is Runnable?

`Runnable` is a **functional interface** (`java.lang.Runnable`) that represents a task to be executed by a thread.

```java
@FunctionalInterface
public interface Runnable {
    void run();
}
```

### 3 Ways to implement

```java
// Way 1: named class
class MyTask implements Runnable {
    public void run() { System.out.println("Task running"); }
}

// Way 2: anonymous class
new Thread(new Runnable() {
    public void run() { System.out.println("Task running"); }
});

// Way 3: lambda (most common)
new Thread(() -> System.out.println("Task running"));
```

### Runnable vs Thread

| | Runnable | Thread |
|---|---|---|
| Type | Interface | Class |
| Can extend other classes? | Yes | No |
| Reusability | Same task, multiple threads | Tied to one thread |
| Preferred? | Yes | Less preferred |

> `Runnable` just defines **what to do** — pass it to a `Thread` or `ExecutorService` to actually run it.

---

## Q3. What is ExecutorService and how is it different from Runnable?

`ExecutorService` is a **higher-level thread management framework** that manages a pool of threads — you submit tasks, it handles the rest.

### The problem it solves
Creating a raw `Thread` for every task is expensive. `ExecutorService` reuses a pool of threads instead.

### Types of thread pools

```java
Executors.newFixedThreadPool(4);      // fixed number of threads
Executors.newCachedThreadPool();      // grows/shrinks dynamically
Executors.newSingleThreadExecutor();  // exactly 1 thread, tasks queued
Executors.newScheduledThreadPool(2);  // run tasks on delay or schedule
```

### execute() vs submit()

```java
// execute() — fire and forget, no return value
executor.execute(() -> saveAuditLog());

// submit() — returns a Future, use when you need the result
Future<String> result = executor.submit(() -> fetchUserFromDB());
String user = result.get();
```

### Runnable vs ExecutorService

| | Runnable | ExecutorService |
|---|---|---|
| What it is | Task definition (interface) | Thread pool manager (interface) |
| Manages threads? | No | Yes |
| Returns a result? | No | Yes, via Future |
| Thread reuse? | No | Yes |
| Use case | Simple one-off tasks | Production-grade concurrent code |

---

## Q4. What is Future?

`Future<T>` is a **placeholder for a result that doesn't exist yet** — it represents the result of an async task still running in the background.

```java
Future<String> order1 = executor.submit(() -> {
    Thread.sleep(100); // running in background
    return "Order #1001 processed";
});

// do other work here...

String result = order1.get(); // NOW block and wait for result
```

### Key methods

```java
future.get()                    // blocks until result is ready
future.get(2, TimeUnit.SECONDS) // wait max 2 seconds, else TimeoutException
future.isDone()                 // true if task finished
future.isCancelled()            // true if cancelled
future.cancel(true)             // attempt to cancel
```

### What can go wrong with get()

```java
try {
    String result = future.get();
} catch (InterruptedException e) {
    Thread.currentThread().interrupt(); // current thread interrupted while waiting
} catch (ExecutionException e) {
    e.getCause(); // the task itself threw an exception
}
```

> **Analogy:** Like an order receipt at a restaurant — you order (submit), get a receipt (Future), collect when ready (`.get()`).

---

## Q5. What does Thread.join() do?

`join()` makes the **calling thread wait** until the thread it's called on finishes.

```java
t1.start();
t1.join(); // main thread waits here until t1 finishes
```

### Visual

```
main thread:   start t1 ----join()----WAIT--------resume-->
t1 thread:               ----running-----------done
```

### Without vs With join()

```java
// WITHOUT — unpredictable order
t1.start();
t2.start();
System.out.println("Done"); // might print BEFORE threads finish

// WITH — guaranteed order
t1.start(); t1.join();
t2.start(); t2.join();
System.out.println("Done"); // always prints AFTER both finish
```

### Overloads

```java
t1.join();        // wait forever
t1.join(2000);    // wait max 2000ms, then continue
```

> **One-liner:** `join()` = *"don't move on until this thread is done."*

---

## Q6. What are the different types of ExecutorService and their use cases?

### 1. newFixedThreadPool(n)
A pool with a fixed number of threads. Extra tasks queue up and wait.

```java
ExecutorService fixed = Executors.newFixedThreadPool(3);
```

**Use case:** Processing orders with a limited DB connection pool — you don't want more threads than connections.

---

### 2. newSingleThreadExecutor()
Exactly one thread. All tasks run sequentially in submission order.

```java
ExecutorService single = Executors.newSingleThreadExecutor();
```

**Use case:** Writing to a single file — concurrent writes would corrupt it, so tasks must go one at a time.

---

### 3. newCachedThreadPool()
Creates new threads as needed, reuses idle ones. No upper limit on thread count.

```java
ExecutorService cached = Executors.newCachedThreadPool();
```

**Use case:** Handling burst HTTP requests — short-lived tasks where traffic is unpredictable.
**Caution:** Can spin up thousands of threads under heavy load.

---

### 4. newScheduledThreadPool(n)
Runs tasks after a delay or on a repeating interval.

```java
ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(2);

// once after 1 second
scheduled.schedule(() -> sendEmail(), 1, TimeUnit.SECONDS);

// every 1 second, starting after 500ms
scheduled.scheduleAtFixedRate(() -> pollApi(), 500, 1000, TimeUnit.MILLISECONDS);
```

**Use case:** Sending a reminder email after signup delay, or polling an external API every 30 seconds.

---

### 5. newVirtualThreadPerTaskExecutor() — Java 21+
Each task gets its own lightweight virtual thread. No pool needed — the JVM manages thousands efficiently.

```java
try (ExecutorService virtual = Executors.newVirtualThreadPerTaskExecutor()) {
    virtual.submit(() -> callExternalApi());
}
```

**Use case:** 10,000 simultaneous I/O-bound tasks (DB calls, API calls) without 10,000 OS threads.

---

### Quick Comparison

| Type | Threads | Best For |
|---|---|---|
| FixedThreadPool | Fixed n | Controlled concurrency (DB limits) |
| SingleThreadExecutor | 1 | Sequential tasks (file writes) |
| CachedThreadPool | Unlimited | Short-lived burst tasks |
| ScheduledThreadPool | Fixed n | Delayed / repeating tasks |
| VirtualThreadPerTask | Virtual (Java 21+) | Massive I/O concurrency |

---

## Q7. What is shutdown() and what is awaitTermination()?

### shutdown()
Tells the executor to **stop accepting new tasks, but finish the ones already submitted.**

```java
executor.shutdown(); // non-blocking — returns immediately
```

- Does **not** wait for tasks to finish
- Does **not** interrupt running tasks
- Just closes the door to new submissions

---

### awaitTermination(timeout, unit)
Blocks the calling thread until **all tasks finish or the timeout expires.**

```java
executor.shutdown();
executor.awaitTermination(5, TimeUnit.SECONDS); // wait max 5 seconds
```

- Returns `true` if all tasks finished within the timeout
- Returns `false` if timeout expired but tasks are still running

---

### shutdown() vs shutdownNow()

```java
executor.shutdown();     // graceful — let running tasks finish
executor.shutdownNow();  // forceful — interrupts running tasks, returns pending ones
```

---

### Full safe shutdown pattern

```java
executor.shutdown();
try {
    if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
        executor.shutdownNow(); // force stop if still running after 5s
    }
} catch (InterruptedException e) {
    executor.shutdownNow();
    Thread.currentThread().interrupt();
}
```

> **One-liner:** `shutdown()` closes the gate, `awaitTermination()` waits for everyone inside to leave.

---

## Q8. What is Thread.sleep()?

`Thread.sleep(ms)` makes the **current thread pause execution for a specified amount of time.**

```java
Thread.sleep(2000); // pause for 2 seconds
```

### Key points
- Pauses the thread that **calls it** — not any other thread
- Thread does **not release any locks** it holds while sleeping
- After the time is up, thread goes back to `RUNNABLE` state (doesn't guarantee exact resume time)

### Overloads

```java
Thread.sleep(2000);        // sleep 2000 milliseconds
Thread.sleep(2000, 500);   // sleep 2000ms + 500 nanoseconds
```

### Why it throws InterruptedException

Another thread can call `t.interrupt()` to wake a sleeping thread early:

```java
try {
    Thread.sleep(2000);
} catch (InterruptedException e) {
    Thread.currentThread().interrupt(); // restore the interrupt flag
}
```

### sleep() vs join()

| | `sleep()` | `join()` |
|---|---|---|
| Who waits | The thread calling sleep | The thread calling join |
| Waits for what | A fixed time | Another thread to finish |
| Releases locks? | No | No |
| Use case | Simulate delay / rate limiting | Wait for another thread's result |

> **One-liner:** `sleep()` = *"pause me for this long, then continue."*

---

## Q9. How does synchronized prevent race conditions with multiple threads?

### What is a race condition?
When two threads read-modify-write the same variable simultaneously, they can overwrite each other's changes.

```java
// count++ is NOT atomic — it's 3 steps:
// 1. read count
// 2. add 1
// 3. write back
// Any thread can cut in between these steps
```

### Without synchronized — broken

```java
static class UnsafeCounter {
    int count = 0;

    void increment() {
        count++; // multiple threads clobber each other
    }
}
// 10 threads x 100 increments = expected 1000, actual: 947, 963, 982... unpredictable
```

### With synchronized — fixed

```java
static class SafeCounter {
    int count = 0;

    synchronized void increment() {
        count++; // only one thread executes this block at a time
    }
}
// 10 threads x 100 increments = always exactly 1000
```

### How synchronized works
- Every Java object has a built-in **monitor lock**
- When a thread enters a `synchronized` method, it **acquires the lock**
- All other threads trying to enter **block and wait**
- When the thread exits, it **releases the lock** — one waiting thread gets it next

### Visual

```
Thread-1: acquire lock → increment → release lock
Thread-2:               waiting...  → acquire lock → increment → release lock
Thread-3:                           waiting...                 → acquire lock → ...
```

### synchronized on a block (more fine-grained)

```java
void increment() {
    synchronized (this) { // lock only this section, not the whole method
        count++;
    }
    // other non-shared code can run freely here
}
```

### Key tradeoff
`synchronized` ensures correctness but reduces parallelism — threads queue up. For high-performance counters prefer `AtomicInteger` instead.

```java
AtomicInteger count = new AtomicInteger(0);
count.incrementAndGet(); // thread-safe without a lock
```

---

## Q10. Is synchronized always used with static?

No. `synchronized` can be used with or without `static` — the difference is **which lock is acquired**.

### Instance-level synchronized (most common)
Locks on `this` — the specific object instance. Two different instances can run concurrently.

```java
class Counter {
    int count = 0;

    synchronized void increment() { // lock = this instance
        count++;
    }
}

Counter a = new Counter();
Counter b = new Counter();
// a.increment() and b.increment() can run in parallel — different locks
```

### Static synchronized
Locks on `ClassName.class` — one lock shared across **all instances**.

```java
class Counter {
    static int count = 0;

    static synchronized void increment() { // lock = Counter.class
        count++;
    }
}
// Only one thread can run Counter.increment() at a time, globally
```

### Comparison

| | Instance `synchronized` | `static synchronized` |
|---|---|---|
| Lock object | `this` (the instance) | `ClassName.class` |
| Scope | Per-object | Across all instances |
| Use when | protecting **instance fields** | protecting **static/shared fields** |
| Two instances parallel? | Yes | No |

> **Rule of thumb:** if the shared data is `static`, use `static synchronized`. If the data belongs to an instance, plain `synchronized` is correct.

---

## Q11. Real-life code examples for instance-level and class-level synchronization

### Instance-level — `BankAccount`

Each account has its **own lock** (`this`). Alice's deposit and Bob's deposit can happen at the same time — they hold different locks. But two threads cannot modify *Alice's* balance simultaneously.

```java
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
    }

    synchronized void withdraw(double amount) {
        if (balance < amount) return;
        balance -= amount;
    }
}

// Alice and Bob transact in parallel — different locks, no blocking each other
BankAccount alice = new BankAccount("Alice", 1000);
BankAccount bob   = new BankAccount("Bob",   500);

pool.execute(() -> alice.deposit(200));  // lock = alice object
pool.execute(() -> bob.deposit(100));    // lock = bob object — runs concurrently with alice's
```

**Why instance lock is right here:** balance belongs to one account. Locking globally would mean depositing into Alice's account blocks Bob's deposit — unnecessary serialization.

---

### Class-level — `OrderIdGenerator`

The counter is `static` — shared across all instances. `static synchronized` locks on `OrderIdGenerator.class`. Without it, two threads could read the same `nextId` value and generate duplicate order IDs.

```java
static class OrderIdGenerator {
    private static int nextId = 1000;

    // Lock = OrderIdGenerator.class — one thread at a time, globally
    static synchronized int generateId() {
        return nextId++;
    }
}

// 10 threads, all get unique IDs — no duplicates
for (int i = 0; i < 10; i++) {
    pool.execute(() -> System.out.println("Order ID: " + OrderIdGenerator.generateId()));
}
// Output: 1000, 1001, 1002 ... 1009 — always unique, never repeated
```

**Why class-level lock is right here:** `nextId` is shared state that doesn't belong to any one instance. Locking on `this` would be meaningless — each `new OrderIdGenerator()` would have its own lock, and threads could still race on the static field.

---

### Side-by-side comparison

| Scenario | Lock | Why |
|---|---|---|
| Bank account balance | Instance (`this`) | Balance belongs to one account; other accounts should not be blocked |
| Order ID counter | Class (`ClassName.class`) | Counter is `static`, shared across all; must be protected globally |

---

## Q12. What is Callable and how is it different from Runnable?

`Callable<V>` is a functional interface like `Runnable`, but it **returns a result** and **can throw a checked exception**.

```java
@FunctionalInterface
public interface Callable<V> {
    V call() throws Exception; // returns V, can throw
}

@FunctionalInterface
public interface Runnable {
    void run();                // returns nothing, cannot throw checked exceptions
}
```

### Key difference in practice

```java
// Runnable — fire and forget, no result
Runnable r = () -> saveAuditLog();
executor.execute(r);

// Callable — you need the result back
Callable<Double> c = () -> fetchPriceFromApi(); // returns a Double
Future<Double> future = executor.submit(c);
Double price = future.get(); // blocks until result is ready
```

### Real-life example

```java
// Fetch product from DB — you need the data, and it can throw SQLException
Callable<Product> fetchProduct = () -> {
    return productRepository.findById(42);
};

Future<Product> result = executor.submit(fetchProduct);
Product p = result.get();
```

You cannot do this with `Runnable` — there is no return slot and no checked exception support.

### Comparison

| | `Runnable` | `Callable<V>` |
|---|---|---|
| Return type | `void` | `V` (any type) |
| Checked exceptions | Cannot throw | Can throw `Exception` |
| Submit via | `execute()` or `submit()` | `submit()` only |
| Returns a `Future`? | `Future<?>` (no useful value) | `Future<V>` (real result) |
| Use when | fire-and-forget tasks | you need a result back |

> **One-liner:** `Runnable` = do something. `Callable` = do something and tell me what you got.

---

## Q13. Real-world code example: Callable vs Runnable

**Scenario:** e-commerce order checkout

- **Callable** — fetch the live product price. The main flow *must* have this value to build the order. The network call can fail, so we need error propagation.
- **Runnable** — send the confirmation email. Fire and forget — the main flow does not care when it finishes or whether it threw.

```java
// CALLABLE: fetch price — result required, failure must surface
Callable<Double> fetchPriceTask = () -> {
    Thread.sleep(500); // simulate network call
    return productId * 9.99;
};

// RUNNABLE: send email — no result needed, runs in background
Runnable sendEmailTask = () -> sendConfirmationEmail("customer@example.com");
```

```java
// Submit Callable → get a Future<Double> to retrieve the result later
Future<Double> priceFuture = executor.submit(fetchPriceTask);

// Execute Runnable → fire and forget, no Future
executor.execute(sendEmailTask);

// Email sends concurrently; we only block where we actually need the price
double price = priceFuture.get();
System.out.println("Order created for $" + price);
```

### Callable surfaces errors — Runnable silently swallows them

```java
// Callable: exception from call() is wrapped in ExecutionException
Future<Double> badFuture = executor.submit(() -> { throw new Exception("API down"); });
try {
    badFuture.get();
} catch (ExecutionException e) {
    System.out.println("Caught: " + e.getCause().getMessage()); // "API down"
}

// Runnable: unchecked exception kills the thread silently — caller never knows
executor.execute(() -> { throw new RuntimeException("silent failure"); });
// nothing caught, nothing returned — the failure disappears
```

### Why this split matters

| Task | Use | Reason |
|---|---|---|
| Fetch live price | `Callable` | Main flow needs the value; call can fail |
| Send email | `Runnable` | Background work; result irrelevant to main flow |
| Write audit log | `Runnable` | Fire and forget |
| Query inventory count | `Callable` | Need to know stock before confirming order |

---

## Q14. How is CompletableFuture different from Future?

`Future` gives you a handle to a result — but getting it requires **blocking**. There is no way to attach "do this next" logic without sitting and waiting.

`CompletableFuture` lets you **chain callbacks** so each step fires automatically when the previous one finishes — the calling thread is never blocked.

### The core problem with Future

```java
Future<Double>  price = executor.submit(() -> fetchPrice(42));
Future<Integer> stock = executor.submit(() -> fetchStock(42));

double p = price.get(); // main thread blocks here
int    s = stock.get(); // then blocks again here
// No way to say "when both are done, combine them" without blocking
```

### CompletableFuture — pipeline, no blocking

```java
CompletableFuture<Double> price = CompletableFuture.supplyAsync(() -> fetchPrice(42));

price
    .thenApply(p -> applyDiscount(p, 0.10))          // transform
    .thenAccept(p -> System.out.println("Price: " + p)) // consume
    .exceptionally(e -> { System.out.println("Failed: " + e.getMessage()); return null; });

// Main thread is free — pipeline runs on its own
```

### Combining two async tasks (not possible cleanly with Future)

```java
CompletableFuture<Double>  priceFuture = CompletableFuture.supplyAsync(() -> fetchPrice(42));
CompletableFuture<Integer> stockFuture = CompletableFuture.supplyAsync(() -> fetchStock(42));

// Fires only when BOTH complete — no blocking
priceFuture.thenCombine(stockFuture, (price, stock) ->
    stock > 0 ? "In stock at $" + price : "Out of stock"
).thenAccept(System.out::println);
```

### Comparison

| | `Future` | `CompletableFuture` |
|---|---|---|
| Get result | `.get()` — **blocks** | `.thenApply()` — **callback, no block** |
| Chain tasks | Not possible | `thenApply → thenAccept → thenRun` |
| Combine two results | Not possible | `thenCombine()`, `allOf()`, `anyOf()` |
| Handle errors inline | Not possible | `exceptionally()`, `handle()` |
| Manual completion | Not possible | `complete(value)` |
| Run async | Via `ExecutorService` | `supplyAsync()` built-in |

> **One-liner:** `Future` = "block until ready." `CompletableFuture` = "here's what to do when it's ready — don't wait."

---

## Q15. All CompletableFuture methods with real-world examples

Scenario: e-commerce order checkout pipeline.

---

### supplyAsync
Runs a task on ForkJoinPool and returns `CompletableFuture<T>`. Use when the task produces a value.

```java
CompletableFuture<String> user = CompletableFuture
    .supplyAsync(() -> fetchUser(1)); // runs in background, returns "Alice"
```

---

### runAsync
Runs a `Runnable` — no return value. Use for fire-and-forget side effects.

```java
CompletableFuture<Void> log = CompletableFuture
    .runAsync(() -> writeAuditLog("checkout started"));
```

---

### thenApply
Transform the result with a `Function<T, R>`. Like `map()` on a stream — produces a new `CompletableFuture<R>`.

```java
CompletableFuture<String> greeting = CompletableFuture
    .supplyAsync(() -> "Alice")
    .thenApply(name -> "Hello, " + name + "!");
```

---

### thenAccept
Consume the result with a `Consumer<T>`. Returns `CompletableFuture<Void>`. Use when you need the value but produce nothing new.

```java
CompletableFuture.supplyAsync(() -> 249.99)
    .thenAccept(price -> System.out.println("Order total: $" + price));
```

---

### thenRun
Run a `Runnable` after completion — no access to the result at all. Use for cleanup, release, or logging after the step is done.

```java
CompletableFuture.supplyAsync(() -> "order-1001")
    .thenRun(() -> System.out.println("pipeline complete, releasing resources"));
```

---

### thenCompose
Chain two async operations where step B depends on step A's result. Like `flatMap()` — avoids nested `CompletableFuture<CompletableFuture<T>>`.

```java
CompletableFuture<String> warehouse = CompletableFuture
    .supplyAsync(() -> 42)                         // get product ID
    .thenCompose(productId -> CompletableFuture    // start next async call with it
        .supplyAsync(() -> fetchInventory(productId)));
```

> **thenApply vs thenCompose:** `thenApply` is for sync transforms (returns `T`). `thenCompose` is for when your transform itself returns a `CompletableFuture<T>`.

---

### thenCombine
Combine two **independent** futures when both complete. Both run in parallel; you merge with a `BiFunction`.

```java
CompletableFuture<Double>  price = CompletableFuture.supplyAsync(() -> fetchPrice(42));
CompletableFuture<Integer> stock = CompletableFuture.supplyAsync(() -> fetchStock(42));

price.thenCombine(stock, (p, s) ->
    String.format("price=$%.2f, stock=%d — %s", p, s, s > 0 ? "CONFIRMED" : "OUT OF STOCK")
);
```

---

### exceptionally
Recover from an exception — provide a fallback value. Only runs if the pipeline threw; skipped on success.

```java
CompletableFuture.<String>supplyAsync(() -> { throw new RuntimeException("gateway down"); })
    .exceptionally(ex -> "retry with saved card: " + ex.getMessage());
```

---

### handle
Always runs — receives `(result, exception)`, one of which is null. Use when you want one place for both success and failure.

```java
CompletableFuture.<String>supplyAsync(() -> { throw new RuntimeException("DB timeout"); })
    .handle((result, ex) -> ex != null
        ? "error: " + ex.getMessage()
        : "success: " + result);
```

> **exceptionally vs handle:** `exceptionally` only fires on failure and returns the same type. `handle` always fires and can return a different type.

---

### whenComplete
Side-effect hook that runs on both success and failure. Does **not** transform the result — the original value/exception passes through unchanged.

```java
CompletableFuture.supplyAsync(() -> 199.99)
    .whenComplete((result, ex) -> {
        if (ex == null) System.out.println("charged: $" + result);
        else            System.out.println("charge failed: " + ex.getMessage());
    });
```

> **whenComplete vs handle:** `whenComplete` is for side effects only. `handle` can transform the result.

---

### complete
Manually push a value into a `CompletableFuture`. Use for cache hits, mocks, or bridging callback-based APIs.

```java
CompletableFuture<String> cf = new CompletableFuture<>();
cf.complete("Alice (from cache)"); // resolves immediately, no async call needed
```

---

### completeExceptionally
Manually fail a `CompletableFuture`. Use to signal a condition was not met (e.g., user banned, validation failed).

```java
CompletableFuture<String> cf = new CompletableFuture<>();
cf.completeExceptionally(new RuntimeException("user banned"));
cf.exceptionally(ex -> "blocked: " + ex.getMessage()).get();
```

---

### allOf
Wait for **all** futures to complete (parallel fan-out). Returns `CompletableFuture<Void>` — call `.join()` on each individually to get values.

```java
CompletableFuture<Void> email = CompletableFuture.runAsync(() -> sendEmail());
CompletableFuture<Void> sms   = CompletableFuture.runAsync(() -> sendSms());
CompletableFuture<Void> push  = CompletableFuture.runAsync(() -> sendPush());

CompletableFuture.allOf(email, sms, push).get(); // waits for all three
System.out.println("all notifications dispatched");
```

---

### anyOf
Complete as soon as **any** future finishes — pick the fastest. Use for redundant calls to multiple servers.

```java
CompletableFuture<Object> wA = CompletableFuture.supplyAsync(() -> { sleep(500); return "Warehouse-A"; });
CompletableFuture<Object> wB = CompletableFuture.supplyAsync(() -> { sleep(100); return "Warehouse-B"; });
CompletableFuture<Object> wC = CompletableFuture.supplyAsync(() -> { sleep(300); return "Warehouse-C"; });

Object fastest = CompletableFuture.anyOf(wA, wB, wC).get(); // "Warehouse-B"
```

---

### Method cheat-sheet

| Method | Input | Output | Use when |
|---|---|---|---|
| `supplyAsync` | `Supplier<T>` | `CF<T>` | async task that returns a value |
| `runAsync` | `Runnable` | `CF<Void>` | async fire-and-forget |
| `thenApply` | `Function<T,R>` | `CF<R>` | sync transform of result |
| `thenAccept` | `Consumer<T>` | `CF<Void>` | consume result, no new value |
| `thenRun` | `Runnable` | `CF<Void>` | post-step action, no result needed |
| `thenCompose` | `T → CF<R>` | `CF<R>` | next step is itself async (flatMap) |
| `thenCombine` | two CFs + `BiFunction` | `CF<R>` | merge two parallel results |
| `exceptionally` | `Function<Throwable,T>` | `CF<T>` | fallback on failure only |
| `handle` | `BiFunction<T,Throwable,R>` | `CF<R>` | handle success and failure together |
| `whenComplete` | `BiConsumer<T,Throwable>` | `CF<T>` | side-effect hook, result passes through |
| `complete` | `T` | `boolean` | manually resolve (cache hit, mock) |
| `completeExceptionally` | `Throwable` | `boolean` | manually fail |
| `allOf` | `CF<?>...` | `CF<Void>` | wait for all to finish |
| `anyOf` | `CF<?>...` | `CF<Object>` | take the first to finish |

---

## Q16. Does CompletableFuture spawn a new thread for callbacks?

**No.** By default, callbacks (`thenApply`, `thenAccept`, `thenRun`, etc.) reuse whatever thread completed the previous stage — no new thread is spawned.

```java
CompletableFuture
    .supplyAsync(() -> fetchPrice(42))  // ForkJoinPool thread
    .thenApply(p -> p * 0.9)           // same ForkJoinPool thread
    .thenAccept(System.out::println);  // same thread again
```

### Edge case 1 — stage already complete when callback is attached
The callback runs on the **calling thread** (e.g., main thread).

```java
CompletableFuture<Integer> cf = CompletableFuture.completedFuture(42); // already done
cf.thenApply(n -> n * 2); // runs on main thread — no pool thread needed
```

### Edge case 2 — *Async variants explicitly submit to a thread pool
`thenApplyAsync`, `thenAcceptAsync`, `thenRunAsync` submit the callback to ForkJoinPool (or a custom executor), guaranteeing it runs on a (possibly different) pool thread.

```java
CompletableFuture
    .supplyAsync(() -> fetchPrice(42))           // ForkJoinPool thread A
    .thenApplyAsync(p -> p * 0.9)               // ForkJoinPool thread B (possibly different)
    .thenApplyAsync(p -> round(p), myExecutor); // myExecutor thread
```

### Why this matters — blocking in a callback

If a callback blocks (DB call, I/O), the non-Async variant ties up the ForkJoinPool thread that completed the previous stage, starving other tasks in the pool.

```java
// DANGEROUS — blocks a ForkJoinPool worker thread
.thenApply(price -> db.save(price))

// SAFE — offloads blocking work to a dedicated executor
.thenApplyAsync(price -> db.save(price), dbExecutor)
```

### Rule of thumb

| Variant | Thread used | Use when |
|---|---|---|
| `thenApply` | Thread that completed the previous stage | fast, non-blocking transforms |
| `thenApplyAsync()` | ForkJoinPool common pool | CPU work on a separate thread |
| `thenApplyAsync(executor)` | Your custom executor | blocking I/O — keep off ForkJoinPool |

Same rule applies to `thenAccept`/`thenRun` and all their `Async` variants.

---

## Q17. CompletableFuture chains cause callback hell — is there an async/await equivalent in Java?

Java has no `async/await` syntax, but **Virtual Threads + Structured Concurrency (Java 21+)** solve the same problem more fundamentally.

### Why JavaScript needed async/await

JavaScript is single-threaded — blocking the event loop kills the whole app. `async/await` lets you write sequential-looking code that yields the thread while waiting.

### Why Java's situation is different

Java is multi-threaded, so blocking was always conceptually fine. The problem was cost: OS threads need ~1 MB stack each, so you couldn't have thousands of them. **Virtual threads** (Java 21) are managed by the JVM — you can have millions, and blocking one is cheap. This means you can write plain blocking code that scales.

### The callback hell problem

```java
// Hard to read, hard to debug
fetchUser(1)
    .thenCompose(user -> fetchPrice(user.productId))
    .thenCompose(price -> fetchStock(price.productId)
        .thenCombine(fetchInventory(price.productId), (stock, wh) -> ...))
    .thenApply(order -> applyDiscount(order))
    .thenCompose(order -> saveOrder(order))
    .exceptionally(...);
```

### The Virtual Thread solution — plain sequential code

```java
try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {

    // fork() = start task in background (like async)
    var price     = scope.fork(() -> fetchPrice(42));
    var stock     = scope.fork(() -> fetchStock(42));
    var warehouse = scope.fork(() -> fetchInventory(42));

    scope.join();           // wait for all (like await Promise.all())
    scope.throwIfFailed();  // surface any exception cleanly

    // get() = read result (like await on individual)
    System.out.println("Price: "     + price.get());
    System.out.println("Stock: "     + stock.get());
    System.out.println("Warehouse: " + warehouse.get());
}
```

No callbacks. No chaining. Normal `try/catch`. Full stack traces.

### Comparison

| | JavaScript | Java (pre-21) | Java 21+ |
|---|---|---|---|
| Mechanism | `async/await` | `CompletableFuture` chains | Virtual Threads + `StructuredTaskScope` |
| Style | looks sync, is async | callback pipeline | looks sync, IS sync (but cheap) |
| Error handling | `try/catch` | `.exceptionally()` / `.handle()` | plain `try/catch` |
| Stack traces | shallow | near useless | full, normal |

### When to still use CompletableFuture

Virtual threads don't replace `CompletableFuture` entirely:
- Use **Virtual Threads** for sequential business logic (request/response flows, order pipelines)
- Use **CompletableFuture** when you genuinely need transform pipelines or reactive data processing
---

## Q19. Write a real-life use case for the Fork/Join framework using RecursiveTask and RecursiveAction

### What is the Fork/Join Framework?

Introduced in Java 7 (`java.util.concurrent`), it is designed for **divide-and-conquer parallelism** — splitting a large task recursively into smaller sub-tasks, executing them on multiple CPU cores, and merging results. It uses a **work-stealing** algorithm: idle threads steal tasks from busy threads' queues, keeping all cores busy.

### Core classes

| Class | Purpose |
|---|---|
| `ForkJoinPool` | Thread pool that manages worker threads |
| `RecursiveTask<V>` | Sub-task that **returns a value** |
| `RecursiveAction` | Sub-task that **returns nothing** (in-place mutation) |

### Key methods

| Method | Meaning |
|---|---|
| `fork()` | Schedule this task asynchronously on another thread |
| `join()` | Wait for this task to complete and return its result |
| `compute()` | Override this — contains the actual logic |
| `invokeAll(t1, t2)` | Fork both tasks and wait for both (shorthand for fork + join) |
| `pool.invoke(task)` | Submit a task and block until it completes |

### Pattern (always the same shape)

```java
protected Result compute() {
    if (chunk is small enough) {
        // BASE CASE: solve directly
        return solveDirectly();
    }
    // DIVIDE: split into sub-tasks
    SubTask left  = new SubTask(leftHalf);
    SubTask right = new SubTask(rightHalf);

    right.fork();                   // async on another thread
    Result leftResult  = left.compute();   // sync on this thread
    Result rightResult = right.join();     // wait for async result

    return merge(leftResult, rightResult);
}
```

### Real-world use case: E-commerce Revenue Calculator

**Scenario**: A platform has 1 million order amounts and needs to:
1. Compute **total revenue** in parallel → `RecursiveTask<Double>`
2. Apply a **10% seasonal discount** to all orders in-place → `RecursiveAction`

```java
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.Random;

public class ForkJoinExample {

    private static final int THRESHOLD = 10_000; // sequential below this size

    // RecursiveTask — divides array, sums each half, merges totals
    static class RevenueCalculator extends RecursiveTask<Double> {
        private final double[] orders;
        private final int start, end;

        RevenueCalculator(double[] orders, int start, int end) {
            this.orders = orders; this.start = start; this.end = end;
        }

        @Override
        protected Double compute() {
            int length = end - start;

            if (length <= THRESHOLD) {          // base case
                double sum = 0;
                for (int i = start; i < end; i++) sum += orders[i];
                return sum;
            }

            int mid = start + length / 2;
            RevenueCalculator left  = new RevenueCalculator(orders, start, mid);
            RevenueCalculator right = new RevenueCalculator(orders, mid,   end);

            right.fork();                        // async on another thread
            double leftResult  = left.compute(); // sync on this thread
            double rightResult = right.join();   // wait for right

            return leftResult + rightResult;
        }
    }

    // RecursiveAction — applies discount to each half in-place (no return value)
    static class DiscountApplier extends RecursiveAction {
        private final double[] orders;
        private final int start, end;
        private final double discountFactor;    // e.g. 0.90 for 10% off

        DiscountApplier(double[] orders, int start, int end, double discountFactor) {
            this.orders = orders; this.start = start;
            this.end = end; this.discountFactor = discountFactor;
        }

        @Override
        protected void compute() {
            int length = end - start;
            if (length <= THRESHOLD) {
                for (int i = start; i < end; i++) orders[i] *= discountFactor;
                return;
            }
            int mid = start + length / 2;
            // invokeAll forks both and waits for both — cleaner than fork+join for actions
            invokeAll(
                new DiscountApplier(orders, start, mid, discountFactor),
                new DiscountApplier(orders, mid,   end, discountFactor)
            );
        }
    }

    public static void main(String[] args) {
        int orderCount = 1_000_000;
        double[] orders = generateOrders(orderCount);

        ForkJoinPool pool = ForkJoinPool.commonPool();
        System.out.println("Parallelism: " + pool.getParallelism() + " threads");

        // Step 1 — total revenue
        double totalRevenue = pool.invoke(new RevenueCalculator(orders, 0, orderCount));
        System.out.printf("Total revenue        : $%,.2f%n", totalRevenue);

        // Step 2 — apply 10% discount in-place
        pool.invoke(new DiscountApplier(orders, 0, orderCount, 0.90));

        // Step 3 — recompute after discount
        double discountedRevenue = pool.invoke(new RevenueCalculator(orders, 0, orderCount));
        System.out.printf("Revenue after 10%% off: $%,.2f%n", discountedRevenue);
        System.out.printf("Ratio (expect ~0.900): %.4f%n", discountedRevenue / totalRevenue);
    }

    private static double[] generateOrders(int count) {
        Random rng = new Random(42);
        double[] orders = new double[count];
        for (int i = 0; i < count; i++) orders[i] = 5.0 + rng.nextDouble() * 495.0;
        return orders;
    }
}
```

### Sample output
```
Parallelism: 8 threads
Processing 1,000,000 orders...

Total revenue        : $250,251,387.43
Discount applied in  : 12 ms
Revenue after 10% off: $225,226,248.69
Ratio (expect ~0.900): 0.9000
```

### fork() vs invokeAll()

| | `right.fork()` + `right.join()` | `invokeAll(left, right)` |
|---|---|---|
| Current thread | Executes `left.compute()` while right runs async | Forks BOTH, current thread waits |
| Best for | `RecursiveTask` where you reuse the current thread for left half | `RecursiveAction` where you don't need to reuse the current thread |

### When to use Fork/Join vs alternatives

| Scenario | Preferred tool |
|---|---|
| CPU-bound divide-and-conquer (sort, sum, scan) | Fork/Join |
| I/O-bound tasks (HTTP calls, DB queries) | `ExecutorService` / `CompletableFuture` |
| Sequential pipeline with transformations | `CompletableFuture` chains |
| Simple parallel loop | `parallelStream()` (uses commonPool internally) |

### `THRESHOLD` tuning
- Too small → too many tasks, thread-coordination overhead dominates
- Too large → not enough parallelism, cores sit idle
- Rule of thumb: `array.length / (4 * nCPUs)` — gives each thread ~4 tasks to steal from

---

## Q20. How do you achieve thread safety in collections? Explain Synchronized vs Concurrent collections with real-world code.

### Why collections are not thread-safe by default

`ArrayList`, `HashMap`, `HashSet` etc. are designed for single-threaded use. When multiple threads read and write concurrently, you get:
- **Lost updates** — two threads overwrite each other's write
- **Inconsistent reads** — one thread reads a partially-written entry
- **ConcurrentModificationException** — a thread modifies a collection while another is iterating it

---

### Two approaches to thread safety

| Approach | How | Package |
|---|---|---|
| **Synchronized wrappers** | Wrap an existing collection; every method acquires one lock on the whole object | `java.util.Collections` |
| **Concurrent collections** | Purpose-built with fine-grained locking or lock-free algorithms | `java.util.concurrent` |

---

### Synchronized Collections — `Collections.synchronizedXxx`

```java
List<String> safeList = Collections.synchronizedList(new ArrayList<>());
Map<String, String> safeMap = Collections.synchronizedMap(new HashMap<>());
Set<String> safeSet  = Collections.synchronizedSet(new HashSet<>());
```

**How it works**: every method (`put`, `get`, `add`, `remove`) is wrapped in `synchronized(this)` — one lock for the whole collection.

#### Limitations

1. **Compound operations are NOT atomic** — two separate synchronized calls are not one atomic action:
   ```java
   // BROKEN — contains() and put() each grab/release the lock separately
   if (!safeMap.containsKey(key)) {
       safeMap.put(key, value);   // another thread can slip in between
   }

   // FIXED — external lock covers both calls together
   synchronized (safeMap) {
       if (!safeMap.containsKey(key)) safeMap.put(key, value);
   }
   ```

2. **Iteration requires an external lock** (or you risk `ConcurrentModificationException`):
   ```java
   synchronized (safeList) {          // must hold this for the whole loop
       for (String s : safeList) { }
   }
   ```

3. **Low throughput under contention** — the single lock serialises all threads.

---

### Concurrent Collections

#### 1. `ConcurrentHashMap` — high-concurrency key-value store

**Locking strategy**: Java 8+ uses CAS (Compare-And-Swap) for most writes and only locks individual buckets when needed — different threads can write to different buckets simultaneously.

**Built-in atomic compound operations** — no external lock ever needed:

```java
ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();

map.putIfAbsent("ORD-1", "PLACED");        // atomic: put only if key absent
map.computeIfAbsent("ORD-2", k -> fetch(k));  // atomic: compute and store if absent
map.computeIfPresent("ORD-1", (k, v) -> "CONFIRMED"); // atomic: update if present
map.merge("COUNT", 1, Integer::sum);        // atomic: insert or accumulate
```

**Real-world scenario** — multiple services (payment, warehouse, delivery) concurrently update order statuses:

```java
Map<String, String> orders = new ConcurrentHashMap<>();
ExecutorService pool = Executors.newFixedThreadPool(4);
String[] statuses = {"PLACED", "CONFIRMED", "SHIPPED", "DELIVERED"};

for (int t = 0; t < 4; t++) {
    final int tid = t;
    pool.submit(() -> {
        for (int i = tid; i < 1000; i += 4) {
            orders.putIfAbsent("ORD-" + i, statuses[tid]);   // no external lock
            orders.merge("COUNT-" + tid, 1, Integer::sum);   // atomic counter
        }
    });
}
pool.shutdown();
pool.awaitTermination(5, TimeUnit.SECONDS);
System.out.println("Total orders tracked: " + orders.size());
```

#### 2. `CopyOnWriteArrayList` — read-heavy lists

**Locking strategy**: every write (`add`, `remove`, `set`) creates a brand-new copy of the backing array under a lock. Reads always see a snapshot and require no lock at all.

**Best for**: listener registries, configuration lists, event subscriptions — cases where reads vastly outnumber writes.

```java
List<String> listeners = new CopyOnWriteArrayList<>();
listeners.add("EmailNotifier");
listeners.add("PushNotifier");

// 5 threads fire events (reads) — completely lock-free, no CME risk
ExecutorService pool = Executors.newFixedThreadPool(5);
for (int i = 0; i < 5; i++) {
    pool.submit(() -> {
        for (String listener : listeners) {   // iterates a snapshot
            notifyListener(listener);         // never throws CME
        }
    });
}

// Writer registers a new listener while readers are running
listeners.add("AuditLogger");    // copies array — existing iterators unaffected
listeners.remove("PushNotifier"); // copies array again
```

**Contrast with `synchronizedList`**: iteration in `synchronizedList` must be wrapped in `synchronized(list){...}` externally, blocking all other threads for the whole loop. `CopyOnWriteArrayList` iteration needs nothing — each reader holds its own snapshot.

#### 3. `LinkedBlockingQueue` — producer-consumer pipelines

**Locking strategy**: uses two separate locks — one for the head (consumer) and one for the tail (producer) — so producer and consumer can run simultaneously.

**Key methods**:

| Method | Behaviour |
|---|---|
| `put(e)` | Inserts; **blocks** if queue is at capacity |
| `take()` | Removes; **blocks** if queue is empty |
| `poll(timeout, unit)` | Removes or returns `null` after timeout |
| `offer(e)` | Inserts; returns `false` immediately if full (non-blocking) |

**Real-world scenario** — order intake feeds fulfillment service:

```java
BlockingQueue<String> queue = new LinkedBlockingQueue<>(5); // max 5 pending orders

// Producer: order intake (faster than consumer)
Thread intake = new Thread(() -> {
    for (int i = 1; i <= 10; i++) {
        try {
            queue.put("ORD-" + i);   // BLOCKS if queue full — natural backpressure
            System.out.println("Queued ORD-" + i + "  queue=" + queue.size());
            Thread.sleep(30);
        } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}, "Intake");

// Consumer: fulfillment (slower)
Thread fulfillment = new Thread(() -> {
    int done = 0;
    while (done < 10) {
        try {
            String order = queue.poll(400, TimeUnit.MILLISECONDS); // BLOCKS if empty
            if (order != null) {
                Thread.sleep(80); // simulate packing
                System.out.println("Processed " + order);
                done++;
            }
        } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}, "Fulfillment");

intake.start(); fulfillment.start();
intake.join();  fulfillment.join();
```

---

### Synchronized vs Concurrent — full comparison

| | `synchronizedXxx` | `ConcurrentHashMap` | `CopyOnWriteArrayList` | `LinkedBlockingQueue` |
|---|---|---|---|---|
| **Lock granularity** | Whole object | Per-bucket / CAS | Whole array (on write) | Per end (head/tail) |
| **Compound ops** | Need external `synchronized` | Built-in (`putIfAbsent`, `merge`…) | N/A | `put`/`take` are inherently atomic |
| **Iteration** | External lock required | Weakly consistent (no CME, may miss live updates) | Lock-free snapshot (no CME) | Iterator is weakly consistent |
| **Null values** | Depends on wrapped type | **Not allowed** (key or value) | Allowed | **Not allowed** |
| **Throughput** | Low (single lock) | High | High reads, low writes | High |
| **Best for** | Low-concurrency, wrapping legacy code | Shared caches, counters, status maps | Listener/config lists | Producer-consumer |

---

### Quick decision guide

```
Shared key-value store, many threads read/write?
  → ConcurrentHashMap

List that is read often, rarely written (listeners, config)?
  → CopyOnWriteArrayList

Threads handing work to other threads (pipeline)?
  → LinkedBlockingQueue (or ArrayBlockingQueue for bounded)

Just need to safely wrap an existing collection, low contention?
  → Collections.synchronizedXxx  (remember: external lock for iteration + compound ops)
```
