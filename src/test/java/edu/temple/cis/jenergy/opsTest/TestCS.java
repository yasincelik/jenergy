package edu.temple.cis.jenergy.opsTest;

import org.junit.Assert;
import org.junit.Test;

import edu.temple.cis.jenergy.computespace.ComputeSpace;
import edu.temple.cis.jenergy.computespace.MatrixTuple;

import java.lang.reflect.Field;


public class TestCS{
	@Test
	public void testingCS() throws InterruptedException {

		ComputeSpace cs = new ComputeSpace();
		//creates the space and starts the context
		Assert.assertTrue(cs.create("INPUT"));
		Assert.assertTrue(cs.create("OUTPUT"));

		int SIZE = 10;
		int G =3;
		
		double[][] A = new double[SIZE][SIZE];
		int numRows = A.length;
		int numCols = A[0].length;

		for (int i = 0; i < numRows; i++)
			for (int j = 0; j < numCols; j++)
				A[i][j] = i * j;

		MatrixTuple tupleA = new MatrixTuple("A",A);
		MatrixTuple tupleB = new MatrixTuple("B",A);
		
		
		Assert.assertTrue(cs.put("INPUT", tupleA, G));
		Assert.assertTrue(cs.put("INPUT", tupleB));

		System.out.println("");
		
		
		//shutdown the context to the compute space
		Assert.assertTrue(cs.close());
	}
}
