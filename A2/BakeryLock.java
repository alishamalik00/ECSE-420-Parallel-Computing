package ca.mcgill.ecse420.a2;

import java.util.concurrent.atomic.AtomicIntegerArray;

//lamport's bakery lock for n-thread mutual exclusion
public class BakeryLock {

  private final int n;
  private final AtomicIntegerArray flag;  //flag[i] = 1 if thread i wants the lock
  private final AtomicIntegerArray label; //label[i] = ticket number for thread i

  //initialize all flags and labels to 0
  public BakeryLock(int n) {
    this.n = n;
    flag = new AtomicIntegerArray(n);
    label = new AtomicIntegerArray(n);
  }

  //acquire lock: doorway takes a ticket, then spins until all lower-priority threads have passed
  public void lock(int me) {
    //doorway: announce interest and take a ticket
    flag.set(me, 1);
    int maxLabel = 0;
    for (int k = 0; k < n; k++) {
      int lk = label.get(k);
      if (lk > maxLabel) maxLabel = lk;
    }
    label.set(me, maxLabel + 1);

    //wait until every other interested thread has lower priority (higher label or higher id)
    for (int k = 0; k < n; k++) {
      if (k == me) continue;

      //wait for thread k to finish its own doorway
      while (flag.get(k) == 1 && label.get(k) == 0) Thread.yield();

      //wait while thread k has strictly higher priority
      while (flag.get(k) == 1 && isLexLess(label.get(k), k, label.get(me), me)) Thread.yield();
    }
  }

  //release lock: clear flag so other threads stop waiting on this thread
  public void unlock(int me) {
    flag.set(me, 0);
  }

  //returns true if (labelA, a) < (labelB, b) lexicographically, meaning A has higher priority
  private boolean isLexLess(int labelA, int a, int labelB, int b) {
    return labelA < labelB || (labelA == labelB && a < b);
  }
}
