package ca.mcgill.ecse420.a1;

/**
 * Deadlock Using Lock Ordering
 * This program demonstrates how to prevent deadlock by ensuring all threads
 * acquire locks in the same order.
 */
public class Deadlock {
    
    private static final Object lockA = new Object();
    private static final Object lockB = new Object();
    
    /**
     * Thread 1 acquires locks in order: A then B
     */
    static class Thread1 extends Thread {
        @Override
        public void run() {
            // Acquire locks in consistent order: A then B
            synchronized (lockA) {
                System.out.println("Thread 1: Lock A acquired");
                
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                System.out.println("Thread 1: Attempting to acquire Lock B");
                
                synchronized (lockB) {
                    System.out.println("Thread 1: Lock B acquired");
                    System.out.println("Thread 1: Performing work with both locks");
                }
            }
            System.out.println("Thread 1: Released both locks");
        }
    }
    
    /**
     * Thread 2 ALSO acquires locks in order: A then B
     * This consistent ordering prevents deadlock
     */
    static class Thread2 extends Thread {
        @Override
        public void run() {
            // Acquire locks in SAME order as Thread 1: A then B
            synchronized (lockA) {
                System.out.println("Thread 2: Lock A acquired");
                
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                System.out.println("Thread 2: Attempting to acquire Lock B");
                
                synchronized (lockB) {
                    System.out.println("Thread 2: Lock B acquired!");
                    System.out.println("Thread 2: Performing work with both locks");
                }
            }
            System.out.println("Thread 2: Released both locks");
        }
    }
    
    public static void main(String[] args) {
        System.out.println("DEADLOCK PREVENTION - LOCK ORDERING SOLUTION");
        System.out.println("\nBoth threads acquire locks in the same order (A then B)\n");
        
        Thread1 t1 = new Thread1();
        Thread2 t2 = new Thread2();
        
        t1.start();
        t2.start();
        
        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("SUCCESS! Both threads completed without deadlock.");
        System.out.println("\nWhy this works:");
        System.out.println("- Both threads acquire locks in the same order (A -> B)");
        System.out.println("- No circular wait is possible");
        System.out.println("- One thread will complete before the other starts");
    }
}
