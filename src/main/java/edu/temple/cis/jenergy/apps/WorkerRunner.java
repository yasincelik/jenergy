package edu.temple.cis.jenergy.apps;

import java.io.IOException;

import edu.temple.cis.jenergy.computespace.ClassPathHacker;


public class WorkerRunner {
	public static void main(String args[]) throws InterruptedException{
		
		try {
			ClassPathHacker.addFile(System.getProperty("user.dir") + "/conf");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Thread workerRunner = new Thread(new MatMulWorker(args[0]));
		workerRunner.run();
		workerRunner.join();
	}
}
