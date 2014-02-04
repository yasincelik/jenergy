package edu.temple.cis.jenergy.appTest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.mortbay.log.Log;

public class TestWorkerFullFailure {

	@Test
	public void TestingWorkerDirtyFailure() throws InterruptedException {

		// ProcessBuilder pb = new ProcessBuilder("java "
		// + "-cp target/Jenergy-0.2.0-jar-with-dependencies.jar:conf"
		// + " edu.temple.cis.jenergy.apps.MasterRunner ");

		String masterCmd = "java -cp Jenergy.jar;conf " +
				"edu.temple.cis.jenergy.apps.MasterRunner" +
				" 3000 10";
		Process master = runCommand(masterCmd);

		ArrayList<Process> listProcs = new ArrayList<Process>();

		int numWorkers = 2;
		

		
		for (int i = 0; i < numWorkers; i++) {
			String workerCmd = "java -cp Jenergy.jar;conf edu.temple.cis.jenergy.apps.WorkerRunner";
			listProcs.add(runCommand(workerCmd));
		}

		
//		TimeUnit.SECONDS.sleep(10);
//		Log.info("Terminating one of the workers");
//		listProcs.get(0).destroy();
		
//		for (int i = 0; i < numWorkers; i++) {
//			//listProcs.get(0).destroy();
//			listProcs.get(i).waitFor();
//		}
//		master.destroy();
		master.waitFor();
	}

	public Process runCommand(String command) {
		Process process = null;
		try {

			String[] commandSplitted = command.split(" ");
			System.out.println(commandSplitted.length);

			process = new ProcessBuilder()
					.inheritIO()
					.command(commandSplitted)
					.directory(
							new File(System.getProperty("user.dir") + "/bin"))
					.start();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return process;
	}
}
