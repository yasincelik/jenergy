package edu.temple.cis.jenergy.appTest;

import java.text.DateFormat;
import java.util.Date;

import org.mortbay.log.Log;

import edu.temple.cis.jenergy.computespace.ComputeSpace;
import edu.temple.cis.jenergy.computespace.MatrixTuple;

public class MatMulWorker extends Thread {

	public String name;
	
	public MatMulWorker(String name){
		this.name = name;
	}
	
	public MatMulWorker(){
		name = new Date().toString();
	}
	
	public void run(){
		try{
		ComputeSpace cs = new ComputeSpace();

		// creates the spaces and starts the context
		cs.open("INPUT");
		cs.open("OUTPUT");

		MatrixTuple B;

		// get the matrix B from the compute space
		

		B = cs.read("INPUT", "B");
		Log.info("Worker:"+name+"got matrix B.");
		
		while (true) {
			MatrixTuple A = cs.get("INPUT", "A");
			if (A == null) {
				// no more work terminate
				System.out
						.println("Worker "+Thread.currentThread().getName()+" Terminating: No more tuples to process.");
				break;
			}
			Log.info("Worker:"+name+"got matrix: "+A.id);
			
			double[][] result;
				
			result = multiply(A.data,B.data);
			
			String resName= "C_"+A.getRequestNum();
			int resStartRow = A.startRow;
			
			MatrixTuple C = new MatrixTuple(resName,result,resStartRow);
			cs.put("OUTPUT", C);
			Log.info("Worker:"+name+"put matrix: "+C.id);
		}
		
		
		System.out.println("Worker:"+name+"done.");
		
		
		}catch (InterruptedException iex) {
		      
			  Log.info("\n\n\n\n---------------------------------------------------\n"
			  +"Worker "+Thread.currentThread().getName()+" was Interrupted:"
			  +"\n---------------------------"
			  +"\n\n\n\n\n\n\n");
			  
			  //Thread [] children = new Thread[Thread.activeCount()];
		      //Thread.enumerate(children);
		      Log.info("hello");
		      return;
		      
	    }
		
	}

	// return C = A * B
    public static double[][] multiply(double[][] A, double[][] B) {
        int mA = A.length;
        int nA = A[0].length;
        int mB = B.length;
        int nB = A[0].length;
        if (nA != mB) throw new RuntimeException("Illegal matrix dimensions.");
        double[][] C = new double[mA][nB];
        for (int i = 0; i < mA; i++)
            for (int j = 0; j < nB; j++)
                for (int k = 0; k < nA; k++)
                    C[i][j] += (A[i][k] * B[k][j]);
        return C;
    }
}
