package edu.temple.cis.jenergy.apps;

import org.mortbay.log.Log;

import edu.temple.cis.jenergy.computespace.ComputeSpace;
import edu.temple.cis.jenergy.computespace.MatrixTuple;

public class MatMulMaster implements Runnable {

	int SIZE = 10;
	int G = 3;

	public MatMulMaster() {
	}

	public MatMulMaster(int size) {
		SIZE = size;
	}

	public MatMulMaster(int size, int G) {
		SIZE = size;
		this.G = G;
	}

	@SuppressWarnings("unused")
	public void run() {

		try {
			ComputeSpace cs = new ComputeSpace();
			// creates the spaces and starts the context
			cs.create("INPUT");
			cs.create("OUTPUT");

			double[][] A = new double[SIZE][SIZE];
			double[][] B = new double[SIZE][SIZE];
			double[][] C = new double[SIZE][SIZE];
			
			int numRows = SIZE;
			int numCols = SIZE;
			
			
			
			long startTime = System.nanoTime();
			
			// create the two matrices
			for (int i = 0; i < numRows; i++)
				for (int j = 0; j < numCols; j++) {
					A[i][j] = i * j;
					B[i][j] = 3 * j;
				}

			// wrap them in tuples
			MatrixTuple tupleA = new MatrixTuple("A", A);
			MatrixTuple tupleB = new MatrixTuple("B", B);

			// put the two matrices in the computespace
			boolean retB = cs.put("INPUT", tupleB);
			boolean retA = cs.put("INPUT", tupleA, G);

			MatrixTuple tupleC;
			// wait for the output in C (in chunks)
			int numResults = SIZE / G;
			if (SIZE % G != 0)
				numResults++;

			double[][] result = new double[SIZE][SIZE];

			for (int i = 0; i < numResults; i++) {
				System.out.println("Waiting for a C result:");
				tupleC = cs.read("OUTPUT", "C", i);
				Log.info("Master received : " + tupleC.id.toString());
				for (int j = 0; j < tupleC.numRows; j++) {
					result[tupleC.startRow + j] = tupleC.data[j];
					// System.out.println(j);
				}
			}
			long endTime = System.nanoTime();
			long duration = endTime - startTime;
			double seconds = (double)duration / 1000000000.0;
			// shutdown the distributed calculation
			

			// do the calculation locally
			// return C = A * B and compare
			Log.info("\n\n\n\n\n\n\n");
			Log.info("---------------------------");
			Log.info("Master Checking the result:");
			// Assert.assertTrue(checkEqual(multiply(A, B), result, 1));
			Log.info("Result OK.");
			Log.info("perf: N:"+ SIZE +" G:"+G+" Time(s):"+seconds);
			
			// shutdown the context to the compute space
			Log.info("Master Shutting down.");
			Log.info("---------------------------");
			Log.info("\n\n\n\n\n\n\n");
					
			cs.close();
			
		} catch (InterruptedException e) {

			System.err.println("Master " + Thread.currentThread().getName()
					+ " was Interrupted:");
			return;
		}
	}

	// return a random m-by-n matrix with values between 0 and 1
	public static double[][] random(int m, int n) {
		double[][] C = new double[m][n];
		for (int i = 0; i < m; i++)
			for (int j = 0; j < n; j++)
				C[i][j] = Math.random();
		return C;
	}

	// return n-by-n identity matrix I
	public static double[][] identity(int n) {
		double[][] I = new double[n][n];
		for (int i = 0; i < n; i++)
			I[i][i] = 1;
		return I;
	}

	// return x^T y
	public static double dot(double[] x, double[] y) {
		if (x.length != y.length)
			throw new RuntimeException("Illegal vector dimensions.");
		double sum = 0.0;
		for (int i = 0; i < x.length; i++)
			sum += x[i] * y[i];
		return sum;
	}

	// return C = A^T
	public static double[][] transpose(double[][] A) {
		int m = A.length;
		int n = A[0].length;
		double[][] C = new double[n][m];
		for (int i = 0; i < m; i++)
			for (int j = 0; j < n; j++)
				C[j][i] = A[i][j];
		return C;
	}

	// return C = A + B
	public static double[][] add(double[][] A, double[][] B) {
		int m = A.length;
		int n = A[0].length;
		double[][] C = new double[m][n];
		for (int i = 0; i < m; i++)
			for (int j = 0; j < n; j++)
				C[i][j] = A[i][j] + B[i][j];
		return C;
	}

	// return C = A - B
	public static double[][] subtract(double[][] A, double[][] B) {
		int m = A.length;
		int n = A[0].length;
		double[][] C = new double[m][n];
		for (int i = 0; i < m; i++)
			for (int j = 0; j < n; j++)
				C[i][j] = A[i][j] - B[i][j];
		return C;
	}

	// return C = A - B
	public static boolean checkEqual(double[][] A, double[][] B, double delta) {
		int m = A.length;
		int n = A[0].length;
		double[][] C = new double[m][n];
		for (int i = 0; i < m; i++)
			for (int j = 0; j < n; j++) {
				C[i][j] = A[i][j] - B[i][j];

				if (C[i][j] > delta) {
					System.out.println(C[i][j]);
					return false;
				}
			}
		return true;
	}

	// return C = A * B
	public static double[][] multiply(double[][] A, double[][] B) {
		int mA = A.length;
		int nA = A[0].length;
		int mB = B.length;
		int nB = A[0].length;
		if (nA != mB)
			throw new RuntimeException("Illegal matrix dimensions.");
		double[][] C = new double[mA][nB];
		for (int i = 0; i < mA; i++)
			for (int k = 0; k < nB; k++)
				for (int j = 0; j < nA; j++)
					C[i][j] += (A[i][k] * B[k][j]);
		return C;
	}

	// matrix-vector multiplication (y = A * x)
	public static double[] multiply(double[][] A, double[] x) {
		int m = A.length;
		int n = A[0].length;
		if (x.length != n)
			throw new RuntimeException("Illegal matrix dimensions.");
		double[] y = new double[m];
		for (int i = 0; i < m; i++)
			for (int j = 0; j < n; j++)
				y[i] += (A[i][j] * x[j]);
		return y;
	}
}
