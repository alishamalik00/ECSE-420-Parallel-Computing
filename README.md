# Assignment 1 — Concurrency & Parallelism Basics

Java exercises exploring core concurrency concepts: deadlock, the Dining
Philosophers problem, and parallel vs. sequential performance. Done as a
two-person team project for ECSE 420 (Parallel Computing).

## What's here

- **Deadlock.java** — a minimal example that deliberately creates a deadlock
  between two threads to demonstrate the conditions that cause one.
- **DiningPhilosophersDeadlock.java** — a version of the classic Dining
  Philosophers problem implemented in a way that can deadlock.
- **DiningPhilosophers.java** — a corrected version that avoids deadlock.
- **MatrixMultiplication.java** — multiplies two large matrices both
  sequentially and in parallel (using a thread pool), then compares
  correctness and measures the speedup.
- **Assignment1_Report.pdf** — the written report analyzing the results.

## What this covers

- Recognizing and reproducing the classic conditions for deadlock
- Fixing a deadlock-prone algorithm (resource ordering / avoidance strategy)
- Measuring real parallel speedup against a sequential baseline, and
  validating that parallelizing didn't change the result
