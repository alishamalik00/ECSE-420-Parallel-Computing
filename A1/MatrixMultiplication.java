package ca.mcgill.ecse420.a1;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MatrixMultiplication {
	
	private static final int NUMBER_THREADS = 4;
	private static final int MATRIX_SIZE = 2000;
	
	public static void main(String[] args) {
		
		// Generate two random matrices, same size
		double[][] a = generateRandomMatrix(MATRIX_SIZE, MATRIX_SIZE);
		double[][] b = generateRandomMatrix(MATRIX_SIZE, MATRIX_SIZE);
		
		// Test and time sequential multiplication
		long startTime = System.nanoTime();
		double[][] seqResult = sequentialMultiplyMatrix(a, b);
		long endTime = System.nanoTime();
		double seqTime = (endTime - startTime) / 1_000_000.0; // Convert to milliseconds
		System.out.println("Sequential execution time: " + seqTime + " ms");
		
		// Test and time parallel multiplication
		startTime = System.nanoTime();
		double[][] parResult = parallelMultiplyMatrix(a, b);
		endTime = System.nanoTime();
		double parTime = (endTime - startTime) / 1_000_000.0;
		System.out.println("Parallel execution time: " + parTime + " ms");
		
		// Validate that both methods produce the same results
		boolean isValid = validateResults(seqResult, parResult);
		System.out.println("Results match: " + isValid);
		
		// Calculate speedup
		double speedup = seqTime / parTime;
		System.out.println("Speedup: " + speedup + "x");
	}
	
	/**
	 * Returns the result of a sequential matrix multiplication
	 * The two matrices are randomly generated
	 * @param a is the first matrix
	 * @param b is the second matrix
	 * @return the result of the multiplication
	 * */
	public static double[][] sequentialMultiplyMatrix(double[][] a, double[][] b) {
		int rowsA = a.length;
		int colsA = a[0].length;
		int colsB = b[0].length;
		
		double[][] result = new double[rowsA][colsB];
		
		// Standard matrix multiplication: C[i][j] = sum of A[i][k] * B[k][j]
		for (int i = 0; i < rowsA; i++) {
			for (int j = 0; j < colsB; j++) {
				double sum = 0.0;
				for (int k = 0; k < colsA; k++) {
					sum += a[i][k] * b[k][j];
				}
				result[i][j] = sum;
			}
		}
		
		return result;
	}
	
	/**
	 * Returns the result of a concurrent matrix multiplication
	 * The two matrices are randomly generated
	 * @param a is the first matrix
	 * @param b is the second matrix
	 * @return the result of the multiplication
	 * */
	public static double[][] parallelMultiplyMatrix(double[][] a, double[][] b) {
		int rowsA = a.length;
		int colsA = a[0].length;
		int colsB = b[0].length;
		
		double[][] result = new double[rowsA][colsB];
		
		// Create thread pool
		ExecutorService executor = Executors.newFixedThreadPool(NUMBER_THREADS);
		
		// Divide work by rows - each task computes one or more complete rows
		int rowsPerThread = (int) Math.ceil((double) rowsA / NUMBER_THREADS);
		
		for (int t = 0; t < NUMBER_THREADS; t++) {
			final int startRow = t * rowsPerThread;
			final int endRow = Math.min(startRow + rowsPerThread, rowsA);
			
			executor.submit(() -> {
				// Each thread computes rows from startRow to endRow
				for (int i = startRow; i < endRow; i++) {
					for (int j = 0; j < colsB; j++) {
						double sum = 0.0;
						for (int k = 0; k < colsA; k++) {
							sum += a[i][k] * b[k][j];
						}
						result[i][j] = sum;
					}
				}
			});
		}
		
		// Shutdown executor and wait for all tasks to complete
		executor.shutdown();
		try {
			executor.awaitTermination(1, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	/**
	 * Validates that two matrices are equal within a small tolerance
	 * @param a first matrix
	 * @param b second matrix
	 * @return true if matrices are equal, false otherwise
	 */
	public static boolean validateResults(double[][] a, double[][] b) {
		if (a.length != b.length || a[0].length != b[0].length) {
			return false;
		}
		
		double tolerance = 1e-6; // Account for floating point precision errors
		
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[0].length; j++) {
				if (Math.abs(a[i][j] - b[i][j]) > tolerance) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Measures execution time for matrix multiplication
	 * @param a first matrix
	 * @param b second matrix
	 * @param isParallel whether to use parallel or sequential method
	 * @return execution time in milliseconds
	 */
	public static double measureExecutionTime(double[][] a, double[][] b, boolean isParallel) {
		long startTime = System.nanoTime();
		
		if (isParallel) {
			parallelMultiplyMatrix(a, b);
		} else {
			sequentialMultiplyMatrix(a, b);
		}
		
		long endTime = System.nanoTime();
		return (endTime - startTime) / 1_000_000.0; // Convert to milliseconds
	}
	
	/**
	 * Populates a matrix of given size with randomly generated integers between 0-10.
	 * @param numRows number of rows
	 * @param numCols number of cols
	 * @return matrix
	 */
	public static double[][] generateRandomMatrix(int numRows, int numCols) {
    		double[][] matrix = new double[numRows][numCols];
    		for (int row = 0; row < numRows; row++) {
        		for (int col = 0; col < numCols; col++) {
            			matrix[row][col] = (int) (Math.random() * 10.0);
        		}
    		}
    		return matrix;
	}

	
}
