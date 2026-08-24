package ca.mcgill.ecse420.a3;

import java.util.concurrent.locks.ReentrantLock;

public class FineGrainedList<T> {

    private Node head;

    public FineGrainedList() {
        head      = new Node(Integer.MIN_VALUE);
        head.next = new Node(Integer.MAX_VALUE);
    }

    public boolean add(T item) {
        int key = item.hashCode();
        head.lock();
        Node pred = head;
        try {
            Node curr = pred.next;
            curr.lock();
            try {
                while (curr.key < key) {
                    pred.unlock();
                    pred = curr;
                    curr = curr.next;
                    curr.lock();
                }
                if (curr.key == key) return false;
                Node node = new Node(item);
                node.next = curr;
                pred.next = node;
                return true;
            } finally {
                curr.unlock();
            }
        } finally {
            pred.unlock();
        }
    }

    public boolean remove(T item) {
        int key = item.hashCode();
        head.lock();
        Node pred = head;
        try {
            Node curr = pred.next;
            curr.lock();
            try {
                while (curr.key < key) {
                    pred.unlock();
                    pred = curr;
                    curr = curr.next;
                    curr.lock();
                }
                if (curr.key == key) {
                    pred.next = curr.next;
                    return true;
                }
                return false;
            } finally {
                curr.unlock();
            }
        } finally {
            pred.unlock();
        }
    }

    public boolean contains(T item) {
        int key = item.hashCode();
        head.lock();
        Node pred = head;
        try {
            Node curr = pred.next;
            curr.lock();
            try {
                while (curr.key < key) {
                    pred.unlock();
                    pred = curr;
                    curr = curr.next;
                    curr.lock();
                }
                return curr.key == key;
            } finally {
                curr.unlock();
            }
        } finally {
            pred.unlock();
        }
    }

    private class Node {
        T item;
        int key;
        Node next;
        private final ReentrantLock lock = new ReentrantLock();

        Node(T item) {
            this.item = item;
            this.key  = item.hashCode();
        }

        Node(int key) {
            this.key = key;
        }

        void lock()   { lock.lock();   }
        void unlock() { lock.unlock(); }
    }
}
