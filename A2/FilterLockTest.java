package ca.mcgill.ecse420.a2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

//tests for the Filter lock: mutual exclusion and overtaking observation
public class FilterLockTest {

  private static final int MUTEX_ITERATIONS = 10_000;  //increments per thread in mutex test
  private static final int OVERTAKE_ITERATIONS = 500;  //lock attempts per thread in overtake test
  private static final int OVERTAKE_THREADS = 8;       //thread count for overtake test (1.4)

  //assigns unique IDs 0..n-1 to threads; must be reset before each test
  private static final AtomicInteger idCounter = new AtomicInteger(0);

  //each thread gets its own unique ID via ThreadLocal
  private static final ThreadLocal<Integer> threadId =
      ThreadLocal.withInitial(() -> idCounter.getAndIncrement());

  public static void main(String[] args) throws InterruptedException {

    //1.2: Mutual exclusion test for n = 2..8
    System.out.println("1.2  Filter Lock: Mutual Exclusion Test (n = 2..8)");
    for (int n = 2; n <= 8; n++) {
      boolean passed = testMutualExclusion(n);
      System.out.printf("  n = %d : %s%n", n, passed ? "PASS" : "FAIL");
    }

    //1.4: Overtaking observation with n = 8
    System.out.println("\n1.4  Filter Lock: Overtaking Observation (n = 8)");
    testOvertaking(OVERTAKE_THREADS);
  }

  //1.2: Mutual exclusion test: n threads each increment a shared counter MUTEX_ITERATIONS times.
  //if no mutual exclusion violation occurs, final value == n * MUTEX_ITERATIONS.
  private static boolean testMutualExclusion(int n) throws InterruptedException {
    idCounter.set(0);
    final int[] counter = {0}; //shared counter: lost updates indicate a mutex violation
    FilterLock lock = new FilterLock(n);

    Thread[] threads = new Thread[n];
    for (int t = 0; t < n; t++) {
      threads[t] = new Thread(() -> {
        int me = threadId.get();
        for (int i = 0; i < MUTEX_ITERATIONS; i++) {
          lock.lock(me);
          counter[0]++; //critical section
          lock.unlock(me);
        }
      });
    }

    for (Thread t : threads) t.start();
    for (Thread t : threads) t.join();

    return counter[0] == n * MUTEX_ITERATIONS;
  }

  //1.4: Overtaking observation test - records the order threads request the lock vs the order they acquire it.
  // An overtake = thread i requested first but thread j acquired first.
  private static void testOvertaking(int n) throws InterruptedException {
    idCounter.set(0);

    AtomicInteger requestSeq = new AtomicInteger(0); //stamped just BEFORE lock()
    AtomicInteger acquireSeq = new AtomicInteger(0); //stamped just AFTER lock() returns
    List<int[]> records = Collections.synchronizedList(new ArrayList<>()); // (request, acquire, id)

    FilterLock lock = new FilterLock(n);

    Thread[] threads = new Thread[n];
    for (int t = 0; t < n; t++) {
      threads[t] = new Thread(() -> {
        int me = threadId.get();
        for (int i = 0; i < OVERTAKE_ITERATIONS; i++) {
          int myRequest = requestSeq.getAndIncrement(); //record request order
          lock.lock(me);
          int myAcquire = acquireSeq.getAndIncrement(); //record acquire order
          records.add(new int[]{myRequest, myAcquire, me});
          lock.unlock(me);
        }
      });
    }

    for (Thread t : threads) t.start();
    for (Thread t : threads) t.join();

    //count pairs where i requested before j but j acquired before i
    int overtakeCount = 0;
    int total = records.size();
    for (int i = 0; i < total; i++) {
      for (int j = 0; j < total; j++) {
        if (i == j) continue;
        int[] ri = records.get(i), rj = records.get(j);
        if (ri[0] < rj[0] && ri[1] > rj[1]) overtakeCount++;
      }
    }

    System.out.printf("  Total lock attempts : %d%n", total);
    System.out.printf("  Overtakes observed  : %d%n", overtakeCount);
    if (overtakeCount > 0) {
      System.out.println("  Overtaking detected: threads were bypassed by later-arriving threads.");
      System.out.println("     This confirms the Filter lock does not guarantee FCFS ordering.");
    } else {
      System.out.println("  No overtaking detected in this run.");
    }
  }
}
