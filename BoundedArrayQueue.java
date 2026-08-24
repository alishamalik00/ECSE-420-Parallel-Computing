package ca.mcgill.ecse420.a3;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

//bounded lock-based queue backed by a circular array with separate head/tail locks
public class BoundedArrayQueue<T> {

    private final T[]           items;     //circular storage array
    private int                 head;      //index of next item to dequeue
    private int                 tail;      //index of next slot to enqueue
    private final int           capacity;
    private final AtomicInteger size;      //current number of items

    private final ReentrantLock headLock;  //guards dequeue operations
    private final Condition     notEmpty;  //dequeuers wait here

    private final ReentrantLock tailLock;  //guards enqueue operations
    private final Condition     notFull;   //enqueuers wait here

    public BoundedArrayQueue(int capacity) {
        this.capacity = capacity;
        this.items    = (T[]) new Object[capacity];
        this.head     = 0;
        this.tail     = 0;
        this.size     = new AtomicInteger(0);

        this.headLock  = new ReentrantLock();
        this.notEmpty  = headLock.newCondition();

        this.tailLock  = new ReentrantLock();
        this.notFull   = tailLock.newCondition();
    }

    //blocking enqueue: acquires tailLock only, waits if queue is full
    public void enq(T x) throws InterruptedException {
        boolean mustWakeDequeuers = false;

        tailLock.lock();
        try {
            while (size.get() == capacity) {
                notFull.await();
            }
            items[tail] = x;
            tail = (tail + 1) % capacity;

            //getAndIncrement returns old value; if 0, queue was empty so wake dequeuers
            if (size.getAndIncrement() == 0) {
                mustWakeDequeuers = true;
            }
        } finally {
            tailLock.unlock();
        }

        //signal after releasing tailLock to avoid holding two locks at once
        if (mustWakeDequeuers) {
            headLock.lock();
            try {
                notEmpty.signalAll();
            } finally {
                headLock.unlock();
            }
        }
    }

    //blocking dequeue: acquires headLock only, waits if queue is empty
    public T deq() throws InterruptedException {
        T result;
        boolean mustWakeEnqueuers = false;

        headLock.lock();
        try {
            while (size.get() == 0) {
                notEmpty.await();
            }
            result = items[head];
            items[head] = null;
            head = (head + 1) % capacity;

            //getAndDecrement returns old value; if capacity, queue was full so wake enqueuers
            if (size.getAndDecrement() == capacity) {
                mustWakeEnqueuers = true;
            }
        } finally {
            headLock.unlock();
        }

        //signal after releasing headLock to avoid holding two locks at once
        if (mustWakeEnqueuers) {
            tailLock.lock();
            try {
                notFull.signalAll();
            } finally {
                tailLock.unlock();
            }
        }
        return result;
    }

    public int size()        { return size.get(); }
    public boolean isEmpty() { return size.get() == 0; }
    public boolean isFull()  { return size.get() == capacity; }

    //correctness test: 4 producers and 4 consumers, verifies all items are consumed
    public static void main(String[] args) throws InterruptedException {
        final int CAPACITY  = 10;
        final int PRODUCERS = 4;
        final int CONSUMERS = 4;
        final int ITEMS_PER_PRODUCER = 50;

        BoundedArrayQueue<Integer> queue =
                new BoundedArrayQueue<>(CAPACITY);

        Thread[] producers = new Thread[PRODUCERS];
        for (int p = 0; p < PRODUCERS; p++) {
            final int pid = p;
            producers[p] = new Thread(() -> {
                for (int i = 0; i < ITEMS_PER_PRODUCER; i++) {
                    try {
                        queue.enq(pid * 1000 + i);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }, "Producer-" + p);
        }

        int[] consumed = new int[CONSUMERS];
        Thread[] consumers = new Thread[CONSUMERS];
        for (int c = 0; c < CONSUMERS; c++) {
            final int cid = c;
            consumers[c] = new Thread(() -> {
                int count = 0;
                while (count < ITEMS_PER_PRODUCER) {
                    try {
                        queue.deq();
                        count++;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                consumed[cid] = count;
            }, "Consumer-" + c);
        }

        for (Thread c : consumers) c.start();
        for (Thread p : producers) p.start();

        for (Thread p : producers) p.join();
        for (Thread c : consumers) c.join();

        int totalConsumed = 0;
        for (int c : consumed) totalConsumed += c;

        System.out.println("Total produced : " + (PRODUCERS * ITEMS_PER_PRODUCER));
        System.out.println("Total consumed : " + totalConsumed);
        System.out.println("Queue empty    : " + queue.isEmpty());
        System.out.println("TEST " + (totalConsumed == PRODUCERS * ITEMS_PER_PRODUCER
                && queue.isEmpty() ? "PASSED" : "FAILED"));
    }
}
