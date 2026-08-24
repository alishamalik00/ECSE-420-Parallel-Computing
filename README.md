# ECSE 420 — Parallel Computing

Coursework from ECSE 420 (Parallel Computing) at McGill University, built
with a partner across three assignments. Each one digs into a different
concurrency problem: from classic textbook issues like deadlock, to
implementing mutual exclusion algorithms from scratch, to building
fine-grained thread-safe data structures and analyzing their performance
under real cache behavior.

## Assignments

### [a1 — Deadlock & Parallel Matrix Multiplication](./a1)
Demonstrates and resolves a deadlock scenario using the Dining
Philosophers problem, then compares sequential vs. parallel matrix
multiplication to measure real speedup from multithreading.

### [a2 — Mutual Exclusion Algorithms](./a2)
Implements the Filter lock and Lamport's Bakery algorithm from first
principles — two different approaches to guaranteeing mutual exclusion
without relying on hardware atomic instructions.

### [a3 — Fine-Grained Concurrency & Cache Performance](./a3)
Builds a concurrently-safe linked list using hand-over-hand locking and a
bounded queue, then analyzes how lock granularity and cache behavior
affect real-world performance at scale.

## Tech
Java, `java.util.concurrent`, multithreading, lock-based synchronization

## Team
Built with a partner as part of coursework; each assignment folder
includes the full written report with performance analysis and results.
