package multithreading.basics;

public class ThreadExample {

    // Extending Thread directly
    static class CounterThread extends Thread {
        private final String label;

        CounterThread(String label) {
            super(label); // sets thread name
            this.label = label;
        }

        @Override
        public void run() {
            for (int i = 1; i <= 3; i++) {
                System.out.println("[" + label + "] count = " + i + " | Thread: " + getName());
                try {
                    Thread.sleep(100); // simulate work
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public static void run() throws InterruptedException {
        Thread threadA = new CounterThread("Thread-A");
        Thread threadB = new CounterThread("Thread-B");

        threadA.start();
        threadB.start();

        // wait for both to finish before continuing
        threadA.join();
        threadB.join();

        System.out.println("[Thread] Both threads finished.");
    }
}