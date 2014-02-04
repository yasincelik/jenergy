package edu.temple.cis.jenergy.appTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.mortbay.log.Log;


public class TestWorkerDirtyFailure {

	@Test
	public void TestingWorkerDirtyFailure() throws InterruptedException {

		
		Thread master = new Thread(new MatMulMaster(1000,100));
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
		
		TimeUnit.SECONDS.sleep(25);
		Log.info("Terminating the worker:"+workers.get(0).getName());
		
		//only terminates the main thread 
		//thus does not terminate the jvm of the thread
		//this leaves the sub threads still up. 
		workers.get(0).interrupt();
		//workers.get(0);
		
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
