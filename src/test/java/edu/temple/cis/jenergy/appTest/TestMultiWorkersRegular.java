package edu.temple.cis.jenergy.appTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;


public class TestMultiWorkersRegular {

	@Test
	public void TestingMultiWorkers() {

		Thread master = new Thread(new MatMulMaster(100,10));
		master.start();
		
		
		try {
			TimeUnit.SECONDS.sleep(10);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		int numWorkers = 2;
		List<Thread> workers = new ArrayList<Thread>(numWorkers);

		for (int i = 0; i < numWorkers; i++) {
			workers.add(new Thread(new MatMulWorker("W:"+i)));
			
		}

		for (int i = 0; i < numWorkers; i++) {
			workers.get(i).start();
		}

		
		
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
