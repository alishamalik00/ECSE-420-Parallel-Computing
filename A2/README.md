# Assignment 2 — Mutual Exclusion Algorithms

Java implementations of two classic software-based mutual exclusion
algorithms, comparing their fairness and performance characteristics.

## What's here

- **FilterLock.java** — implements Peterson's/Filter lock, a generalization
  of mutual exclusion to N threads using a level-based waiting scheme.
- **BakeryLock.java** — implements Lamport's Bakery algorithm, which uses
  ticket numbers to guarantee first-come-first-served fairness among threads.
- **FilterLockTest.java / BakeryLockTest.java** — test drivers that exercise
  each lock under concurrent access.

## What this covers

- Implementing mutual exclusion without relying on hardware atomic
  instructions
- Comparing fairness guarantees between different locking algorithms
- Testing concurrent code for correctness under contention
