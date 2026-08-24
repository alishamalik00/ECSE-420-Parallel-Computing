package ca.mcgill.ecse420.a2;

import java.util.concurrent.atomic.AtomicIntegerArray;

//filter lock for n-thread mutual exclusion
public class FilterLock {

  private final int n;
  private final AtomicIntegerArray level;  //level[i] = current level of thread i
  private final AtomicIntegerArray victim; //victim[L] = last thread to set itself victim at level L

  //initialize both arrays to zero for all n threads
  public FilterLock(int n) {
    this.n = n;
    level = new AtomicIntegerArray(n);
    victim = new AtomicIntegerArray(n);
  }

  //acquire lock: thread advances through levels 1..n-1, spinning at each until no longer victim
  public void lock(int me) {
    for (int L = 1; L < n; L++) {
      level.set(me, L);
      victim.set(L, me);

      //spin while another thread is at level >= L and this thread is still the victim
      boolean waiting;
      do {
        waiting = false;
        for (int k = 0; k < n; k++) {
          if (k != me && level.get(k) >= L && victim.get(L) == me) {
            waiting = true;
            break;
          }
        }
      } while (waiting);
    }
  }

  //release lock: reset level to 0 so this thread is invisible to all spinners
  public void unlock(int me) {
    level.set(me, 0);
  }
}
