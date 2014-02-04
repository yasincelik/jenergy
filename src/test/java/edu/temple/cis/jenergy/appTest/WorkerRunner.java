package edu.temple.cis.jenergy.appTest;

import org.junit.Assert;
import org.mortbay.log.Log;

import edu.temple.cis.jenergy.computespace.*;

public class WorkerRunner {
	public static void main(String args[]){
		Thread workerRunner = new Thread(new MatMulWorker());
		workerRunner.run();
	}
}
