package edu.temple.cis.jenergy.appTest;


public class MasterRunner {

	
	public static void main(String args[]){
		
		args = new String[2];
		args[0]="1000";
		args[1]="100";
		
		
		
		Thread masterRunner = new Thread(new MatMulMaster(Integer.parseInt(args[0]),Integer.parseInt(args[1])));
		masterRunner.run();
		
		
	}

	
}
