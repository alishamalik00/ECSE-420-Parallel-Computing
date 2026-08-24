package ca.mcgill.ecse420.a3;

import java.util.concurrent.*;
import java.util.*;

//parallel matrix-vector multiplication using ForkJoinPool
//sequential: O(n^2) work, parallel: work Theta(n^2), critical path Theta(log n)
public class MatrixVectorMult {

    static ForkJoinPool pool;
    static int THRESHOLD = 64;  //sub-ranges at or below this size are computed sequentially

    //sequential matrix-vector multiply: c = A * x
    public static double[] seqMultiply(double[][] A, double[] x) {
        int n = x.length;
        double[] c = new double[n];
        for (int i = 0; i < n; i++) {
            double sum = 0.0;
            for (int j = 0; j < n; j++) sum += A[i][j] * x[j];
            c[i] = sum;
        }
        return c;
    }

    //recursive parallel dot product for a single row over columns [lo, hi)
    static class DotTask extends RecursiveTask<Double> {
        final double[][] A; final double[] x;
        final int row, lo, hi;
        DotTask(double[][] A, double[] x, int row, int lo, int hi) {
            this.A=A; this.x=x; this.row=row; this.lo=lo; this.hi=hi;
        }
        protected Double compute() {
            int w = hi - lo;
            if (w <= THRESHOLD) {
                double s = 0;
                for (int j=lo; j<hi; j++) s += A[row][j] * x[j];
                return s;
            }
            int mid = lo + w/2;
            DotTask left  = new DotTask(A, x, row, lo,  mid);
            DotTask right = new DotTask(A, x, row, mid, hi);
            left.fork();
            double r = right.compute();
            return left.join() + r;
        }
    }

    //recursively fans out across rows [rowLo, rowHi), writing results into c[]
    static class RowsTask extends RecursiveAction {
        final double[][] A; final double[] x; final double[] c;
        final int rowLo, rowHi;
        RowsTask(double[][] A, double[] x, double[] c, int rowLo, int rowHi) {
            this.A=A; this.x=x; this.c=c; this.rowLo=rowLo; this.rowHi=rowHi;
        }
        protected void compute() {
            if (rowHi - rowLo == 1) {
                c[rowLo] = new DotTask(A, x, rowLo, 0, x.length).compute();
                return;
            }
            int mid = rowLo + (rowHi - rowLo) / 2;
            RowsTask lo = new RowsTask(A, x, c, rowLo, mid);
            RowsTask hi = new RowsTask(A, x, c, mid,   rowHi);
            lo.fork();
            hi.compute();
            lo.join();
        }
    }

    //parallel matrix-vector multiply: submits all rows as concurrent ForkJoin tasks
    public static double[] parMultiply(double[][] A, double[] x) {
        int n = x.length;
        double[] c = new double[n];
        pool.invoke(new RowsTask(A, x, c, 0, n));
        return c;
    }

    //returns true if arrays a and b agree within 1e-6 relative tolerance
    static boolean approxEqual(double[] a, double[] b) {
        for (int i=0; i<a.length; i++)
            if (Math.abs(a[i]-b[i]) > 1e-6 * (Math.abs(a[i]) + 1)) return false;
        return true;
    }

    static double[][] randomMatrix(int n, Random rng) {
        double[][] A = new double[n][n];
        for (int i=0; i<n; i++) for (int j=0; j<n; j++) A[i][j] = rng.nextDouble()*2 - 1;
        return A;
    }

    static double[] randomVector(int n, Random rng) {
        double[] x = new double[n];
        for (int j=0; j<n; j++) x[j] = rng.nextDouble()*2 - 1;
        return x;
    }

    //validates sequential and parallel results against a known 3x3 answer and a random 512x512 input
    static void validateCorrectness() throws Exception {
        System.out.println("4.1  Correctness Validation");

        double[][] A3 = {{1,2,3},{4,5,6},{7,8,9}};
        double[]   x3 = {1,1,1}, exp3 = {6,15,24};
        double[]   s3 = seqMultiply(A3, x3);
        int savedThr = THRESHOLD; THRESHOLD = 1;
        double[]   p3 = parMultiply(A3, x3);
        THRESHOLD = savedThr;
        System.out.println("3x3 known test:");
        System.out.println("Expected  : " + Arrays.toString(exp3));
        System.out.println("Sequential: " + Arrays.toString(s3));
        System.out.println("Parallel  : " + Arrays.toString(p3));
        System.out.println("Pass: " + (Arrays.equals(s3, exp3) && approxEqual(p3, exp3)));

        Random rng = new Random(7);
        int n = 512; double[][] B = randomMatrix(n, rng); double[] v = randomVector(n, rng);
        double[] sB = seqMultiply(B, v);
        THRESHOLD = 32; double[] pB = parMultiply(B, v); THRESHOLD = savedThr;
        System.out.printf("Random %dx%d: match=%b%n%n", n, n, approxEqual(sB, pB));
    }

    //benchmarks sequential and parallel multiply for n=4000 across a range of threshold values
    static void benchmark(int n) throws Exception {
        System.out.printf("4.3  Benchmark  n=%d %n", n);
        int P = pool.getParallelism();
        System.out.println("Thread pool parallelism (P): " + P);

        Random rng = new Random(42);
        double[][] A = randomMatrix(n, rng); double[] x = randomVector(n, rng);

        final int WARMUP = 3, RUNS = 5;

        for (int r=0; r<WARMUP; r++) seqMultiply(A, x);
        long seqNs = 0;
        for (int r=0; r<RUNS; r++) { long t = System.nanoTime(); seqMultiply(A, x); seqNs += System.nanoTime() - t; }
        double seqMs = seqNs / 1e6 / RUNS;
        System.out.printf("Sequential avg: %.1f ms%n%n", seqMs);

        int[] thresholds = { n, n/2, n/4, n/8, 128, 64, 32 };
        System.out.printf("%-10s %-14s %-10s %-14s%n", "Threshold", "Par avg(ms)", "Speedup", "#Tasks(est)");
        System.out.println("-".repeat(52));

        int bestThr = n; double bestSpeedup = 0;
        for (int thr : thresholds) {
            if (thr < 1) thr = 1;
            THRESHOLD = thr;
            for (int r=0; r<WARMUP; r++) parMultiply(A, x);
            long parNs = 0;
            for (int r=0; r<RUNS; r++) { long t = System.nanoTime(); parMultiply(A, x); parNs += System.nanoTime() - t; }
            double parMs = parNs / 1e6 / RUNS;
            double speedup = seqMs / parMs;
            long tasks = 2L * (n / thr) * n + 2L * n;
            if (speedup > bestSpeedup) { bestSpeedup = speedup; bestThr = thr; }
            System.out.printf("%-10d %-14.1f %-10.2f %-14d%n", thr, parMs, speedup, tasks);
        }

        THRESHOLD = bestThr;
        double[] cSeq = seqMultiply(A, x), cPar = parMultiply(A, x);
        System.out.printf("%nOptimal threshold: %d  (speedup %.2fx)%n", bestThr, bestSpeedup);
        System.out.printf("Result correct at optimal threshold: %b%n", approxEqual(cSeq, cPar));
    }

    //prints work, critical path, and parallelism analysis for n=4000
    static void printAnalysis(int n) {
        int P = pool.getParallelism();
        double logN = Math.log(n) / Math.log(2);
        System.out.println("\n4.4  Work and Critical-Path Analysis");
        System.out.printf("W(n) = Theta(n^2):  for n=%d, W = %d%n", n, (long)n*n);
        System.out.printf("D(n) = Theta(log n): for n=%d, D = %.0f steps%n", n, 2*logN);
        System.out.printf("Parallelism = W/D = Theta(n^2/log n): for n=%d, = %.0f%n", n, (double)n*n/(2*logN));
        System.out.printf("Available P=%d threads; practical speedup bounded by min(P, parallelism) = P%n", P);
    }

    public static void main(String[] args) throws Exception {
        int P = Runtime.getRuntime().availableProcessors();
        pool = new ForkJoinPool(P);
        System.out.println("ForkJoinPool parallelism: " + P + "\n");

        validateCorrectness();

        int n = 4000;
        benchmark(n);
        printAnalysis(n);

        pool.shutdown();
    }
}
