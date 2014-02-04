package edu.temple.cis.jenergy.apps;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.Random;

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
		cs.open("INPUT");
		cs.open("OUTPUT");

		MatrixTuple B;

		B = cs.read("INPUT", "B");
		Log.info("Worker:"+name+" got matrix B.");
		Log.info("Worker: processing received matrix");
		
		while (true) {
			MatrixTuple A = cs.get("INPUT", "A");
			if (A == null) {
				System.out
						.println("Worker "+Thread.currentThread().getName()+" Terminating: No more tuples to process.");
				break;
			}
			try {
                //System.out.println("Worker:"+name+"got matrix: "+A.id);
				executor.execute(new Multiply(cs, A, B));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("Worker:"+name+" done.");
		
		}catch (InterruptedException iex) {
		      return;
	    }
            return;
	}

    final int corePoolSize = 1;
	final int maximumPoolSize =1;
	final int keepAliveTime = 5000;
	BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();
	
	Multiplyer executor = 
			new Multiplyer(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.MILLISECONDS, workQueue);
    
    public  class Multiplyer extends ThreadPoolExecutor 
	{
		public Multiplyer(int corePoolSize, int maximumPoolSize,
				long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
			super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
		}

		@Override
		protected void beforeExecute(Thread t, Runnable r) {
			super.beforeExecute(t, r);
		}

		@Override
		protected void afterExecute(Runnable r, Throwable t) {
			super.afterExecute(r, t);
			if (t != null) {
			}
		}

	}
    
    protected class Multiply extends Thread
	{
    	private ComputeSpace cs;
    	private String resName;
    	private int resStartRow;
    	private MatrixTuple A;
    	private MatrixTuple B;
    	
		public Multiply(ComputeSpace cs, MatrixTuple A, MatrixTuple B) throws IOException
		{
			this.cs = cs;
			this.resName = "C_"+A.getRequestNum();
			this.resStartRow = A.startRow;;
			this.A = A;
			this.B = B;
		}

		public double[][] multiply(double[][] A, double[][] B) {
	        int mA = A.length;
	        int nA = A[0].length;
	        int mB = B.length;
	        int nB = A[0].length;
	        if (nA != mB) throw new RuntimeException("Illegal matrix dimensions.");
	        double[][] C = new double[mA][nB];
                for (int k = 0; k < nB; k++)
                   // for (int j = 0; j < nA; j++)
                    for (int i = 0; i < mA; i++)
	                for (int j = 0; j < nA; j++)
	                    C[i][j] += (A[i][k] * B[k][j]);
	        return C;
	    }
		
		public void run()
		{
                        Random rand = new Random(); 
                        int num = rand.nextInt();
			double[][] result;
                        //System.out.println("TimeComputationStart for:"+num+" - "+System.currentTimeMillis());
			result = multiply(A.data,B.data);
                        //System.out.println("TimeComputationEnd for :"+num +" - "+System.currentTimeMillis());
			MatrixTuple C = new MatrixTuple(resName,result,resStartRow);
			try {
				cs.put("OUTPUT", C);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}

