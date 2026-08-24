package ca.mcgill.ecse420.a1;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Dining Philosophers problem - prevents deadlock using asymmetric ordering.
 */
public class DiningPhilosophers {
    /**
     * Main method to run the dining philosophers simulation.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        int numberOfPhilosophers = 5;
        Philosopher[] philosophers = new Philosopher[numberOfPhilosophers];
        ReentrantLock[] chopsticks = new ReentrantLock[numberOfPhilosophers];

        // initialize the chopsticks as locks
        for (int i = 0; i < numberOfPhilosophers; i++) {
            chopsticks[i] = new ReentrantLock(true);
        }

        //this is creating philosopher threads
        for (int i = 0; i < numberOfPhilosophers; i++) {
            ReentrantLock leftChopstick = chopsticks[i];
            ReentrantLock rightChopstick = chopsticks[(i + 1) % numberOfPhilosophers];

            //last philosopher picks up right first then left to break circular wait
            if (i == numberOfPhilosophers - 1) {
                philosophers[i] = new Philosopher(i, rightChopstick, leftChopstick);
            } else {
                philosophers[i] = new Philosopher(i, leftChopstick, rightChopstick);
            }
        }

        //starting all the threads
        for (int i = 0; i < numberOfPhilosophers; i++) {
            Thread t = new Thread(philosophers[i]);
            t.start();
        }
    }

    /**
     * Represents a philosopher who alternates between thinking and eating.
     */
    public static class Philosopher implements Runnable {
        private int philosopherId;
        private ReentrantLock firstChopstick;
        private ReentrantLock secondChopstick;
        private int timesEaten;

        /**
         * Creates a new Philosopher.
         *
         * @param id philosopher identifier
         * @param first first chopstick to acquire
         * @param second second chopstick to acquire
         */
        public Philosopher(int id, ReentrantLock first, ReentrantLock second) {
            this.philosopherId = id;
            this.firstChopstick = first;
            this.secondChopstick = second;
            this.timesEaten = 0;
        }

        /**
         * Executes the philosopher's think-eat cycle.
         */
        @Override
        public void run() {
            try {
                while (true) {
                    // thinking phase
                    think();

                    //pick up first chopstick
                    firstChopstick.lock();
                    System.out.println("Philosopher " + philosopherId + " picked up first chopstick");

                    //pick up second chopstick
                    secondChopstick.lock();
                    System.out.println("Philosopher " + philosopherId + " picked up second chopstick");

                    //here is the eating phase
                    eat();
                    timesEaten = timesEaten + 1;
                    System.out.println("Philosopher " + philosopherId + " finished eating (total: " + timesEaten + " times)");

                    //release second chopstick
                    secondChopstick.unlock();

                    //release first chopstick
                    firstChopstick.unlock();
                    System.out.println("Philosopher " + philosopherId + " put down chopsticks");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        /**
         * This is the function for thinking.
         *
         * @throws InterruptedException if interrupted
         */
        private void think() throws InterruptedException {
            System.out.println("Philosopher " + philosopherId + " is thinking");
            Thread.sleep((long)(Math.random() * 150));
        }

        /**
         * This is the function for eating.
         *
         * @throws InterruptedException if interrupted
         */
        private void eat() throws InterruptedException {
            System.out.println("Philosopher " + philosopherId + " is eating");
            Thread.sleep((long)(Math.random() * 150));
        }
    }
}
