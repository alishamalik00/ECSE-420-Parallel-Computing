package ca.mcgill.ecse420.a1;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Dining Philosophers problem for Q3.1.
 * This version does NOT avoid deadlock on purpose.
 * All philosophers pick up the left chopstick first, then the right.
 * This can eventually lead to the classic circular-wait deadlock.
 */
public class DiningPhilosophers {

  public static void main(String[] args) {
    int numberOfPhilosophers = 5; // should work for any n >= 2
    Philosopher[] philosophers = new Philosopher[numberOfPhilosophers];
    ReentrantLock[] chopsticks = new ReentrantLock[numberOfPhilosophers];

    // Chopsticks are shared objects (locks) so two philosophers cannot hold the same chopstick.
    // Use default (unfair) or fair, deadlock is still possible either way.
    for (int i = 0; i < numberOfPhilosophers; i++) {
      chopsticks[i] = new ReentrantLock(); // intentionally not solving starvation/deadlock here
      // chopsticks[i] = new ReentrantLock(true); // also fine, still can deadlock
    }

    // Everyone uses the SAME acquisition order: left then right.
    // This makes deadlock possible.
    for (int i = 0; i < numberOfPhilosophers; i++) {
      ReentrantLock left = chopsticks[i];
      ReentrantLock right = chopsticks[(i + 1) % numberOfPhilosophers];
      philosophers[i] = new Philosopher(i, left, right);
    }

    for (int i = 0; i < numberOfPhilosophers; i++) {
      new Thread(philosophers[i], "Philosopher-" + i).start();
    }
  }

  public static class Philosopher implements Runnable {
    private final int id;
    private final ReentrantLock leftChopstick;
    private final ReentrantLock rightChopstick;
    private int timesEaten = 0;

    public Philosopher(int id, ReentrantLock leftChopstick, ReentrantLock rightChopstick) {
      this.id = id;
      this.leftChopstick = leftChopstick;
      this.rightChopstick = rightChopstick;
    }

    @Override
    public void run() {
      try {
        while (true) {
          think();

          // Intentionally deadlock-prone order:
          // pick up left first, then right.
          leftChopstick.lock();
          System.out.println("Philosopher " + id + " picked up LEFT");

          // Small delay makes the deadlock scenario show up faster in practice.
          Thread.sleep(1);

          rightChopstick.lock();
          System.out.println("Philosopher " + id + " picked up RIGHT");

          eat();
          timesEaten++;
          System.out.println("Philosopher " + id + " finished eating (total: " + timesEaten + ")");

          rightChopstick.unlock();
          leftChopstick.unlock();
          System.out.println("Philosopher " + id + " put down chopsticks");
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }

    private void think() throws InterruptedException {
      System.out.println("Philosopher " + id + " is thinking");
      Thread.sleep((long) (Math.random() * 150));
    }

    private void eat() throws InterruptedException {
      System.out.println("Philosopher " + id + " is eating");
      Thread.sleep((long) (Math.random() * 150));
    }
  }
}
