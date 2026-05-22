import multithreading.basics.RunnableExample;
import multithreading.basics.ThreadExample;
import multithreading.basics.ExecutorServiceExample;
import multithreading.basics.ExecutorServiceTypesExample;
import multithreading.basics.SynchronizedExample;

import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        System.out.println("========== Java All Along ==========\n");

        System.out.println("---------- 1. Runnable ----------");
        RunnableExample.run();

        System.out.println("\n---------- 2. Thread ----------");
        ThreadExample.run();

        System.out.println("\n---------- 3. ExecutorService: execute() vs submit() ----------");
        ExecutorServiceExample.run();

        System.out.println("\n---------- 4. ExecutorService Types ----------");
        ExecutorServiceTypesExample.run();

        System.out.println("\n---------- 5. Synchronized: Race Condition ----------");
        SynchronizedExample.run();
    }
}