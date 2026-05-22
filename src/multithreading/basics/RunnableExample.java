package multithreading.basics;

public class RunnableExample {

    // Way 1: implement Runnable in a class
    static class PrintTask implements Runnable {
        private final String message;

        PrintTask(String message) {
            this.message = message;
        }

        @Override
        public void run() {
            System.out.println("[Class] " + message + " | Thread: " + Thread.currentThread().getName());
        }
    }

    public static void run() throws InterruptedException {
        // Way 1: named class
        Thread t1 = new Thread(new PrintTask("Hello from named class"));
        t1.start();
        t1.join();// blocks here till get the result

        // Way 2: anonymous class
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("[Anonymous] Running | Thread: " + Thread.currentThread().getName());
            }
        });
        t2.start();
        t2.join();

        // Way 3: lambda (most common)
        Thread t3 = new Thread(() ->
            System.out.println("[Lambda] Running | Thread: " + Thread.currentThread().getName())
        );
        t3.start();
        t3.join();
    }
}