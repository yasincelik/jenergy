package edu.temple.cis.jenergy.apps;


import java.io.IOException;

import org.mortbay.log.Log;

import edu.temple.cis.jenergy.computespace.ClassPathHacker;

public class MasterRunner {

	public static void main(String args[]) throws InterruptedException {

		try {
			ClassPathHacker.addFile(System.getProperty("user.dir") + "/conf");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (args == null) {
			args = new String[2];
			args[0] = "1100";
			args[1] = "100";
		}
		Log.info("Running the master matmul: ");
		Thread masterRunner = new Thread(new MatMulMaster(
				Integer.parseInt(args[0]), Integer.parseInt(args[1])));
		masterRunner.run();
		masterRunner.join();

	}

}
