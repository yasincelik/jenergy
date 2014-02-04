package edu.temple.cis.jenergy.appTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.mortbay.log.Log;

import edu.temple.cis.jenergy.computespace.RecoveryManager;


public class TestMasterFailure {

	@Test
	public void TestingWorkerDirtyFailure() throws InterruptedException {

		
		MatMulMaster master = new MatMulMaster(1000,10);
		master.start();

		
		//TimeUnit.SECONDS.sleep(10);
		Log.info("Started the master.");
		
		int numWorkers = 2;
		
		List<Thread> workers = new ArrayList<Thread>(numWorkers);

		for (int i = 0; i < numWorkers; i++) {
			Log.info("Started the worker: "+i);
			workers.add(new Thread(new MatMulWorker("Worker:"+i)));
			workers.get(i).setName("Worker:"+ i);
		}
		

		for (int i = 0; i < numWorkers; i++) {
			workers.get(i).start();
		}

		//sleep  and terminate the first  worker
		
		//TimeUnit.SECONDS.sleep(25);

		//only terminates the main thread 
		//thus does not terminate the jvm of the thread
		//this leaves the sub threads still up. 
		master.interrupt();
		master.cs.closeForTesting();
		
		
		Log.info("\n\n\n\n\n\n--------------------------------" +
				"Terminated the master" +
				"\n---------------------------\n\n\n\n\n");
		
		TimeUnit.SECONDS.sleep(5);
		//set recovery 
		RecoveryManager recov = new RecoveryManager();
		
		Log.info("Recovery status is "+ recov.checkRecoveryStatus());
		Log.info("Setting recovery status to true");
		recov.setRecoveryStatus(true);
		Log.info("Recovery status is "+ recov.checkRecoveryStatus());
		
		master = new MatMulMaster(1000,10);
		
		master.start();
		
		Log.info("\n\n\n\n\n\n--------------------------------" +
				"Restarted the master" +
				"\n---------------------------\n\n\n\n\n");
		
		
//		
		try {
			for (Thread t : workers)
				t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		try {
			master.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		

	}

}
