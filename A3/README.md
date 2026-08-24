# Assignment 3 — Fine-Grained Concurrency & Cache Performance

Java implementations exploring fine-grained locking and parallel data
structures, along with an analysis of cache behavior and lock performance.

## What's here

- **FineGrainedList.java** — a linked list using hand-over-hand locking,
  where each node has its own lock so multiple threads can operate on
  different parts of the list concurrently.
- **FineGrainedListTest.java** — tests validating correctness of concurrent
  `contains()` operations under multiple threads.
- **BoundedArrayQueue.java** — a bounded, array-backed concurrent queue.
- **MatrixVectorMult.java** — matrix-vector multiplication used as part of
  the cache/performance analysis.
- **Assignment3_Report.pdf** — written analysis covering cache capacity
  thresholds, cache hit vs. memory access latency, and how lock padding
  affects performance under different array sizes.

## What this covers

- Fine-grained (hand-over-hand) locking vs. coarse-grained locking
- Designing and testing thread-safe data structures
- Reasoning about cache behavior and its effect on concurrent performance
