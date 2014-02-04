package edu.temple.cis.jenergy.computespace;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.*;

public class CSConfiguration {
	private String path;
	private String ip;

	private boolean help;
	Options options = new Options();

	// create Options object
	public CSConfiguration(String args[]) {

		// add t option
		options.addOption("ip", true, "specify IP to use");
		options.addOption("path", true, "root path to use");
		options.addOption("help", false, "print the usage");
		
		CommandLineParser parser = new GnuParser();
		CommandLine cmd = null;
		HelpFormatter formatter = new HelpFormatter();
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
		
		if (cmd.hasOption("help")) {
			help = true;
			formatter.printHelp( "startCS", options );
			System.exit(1);
		}
		
		
		
		if (cmd.hasOption("ip")) {
			ip = cmd.getOptionValue("ip");
		}else{
			//stop launch
			
			formatter.printHelp( "startCS", options );
			System.exit(1);
		}
			

		if (cmd.hasOption("path")) {
			path = cmd.getOptionValue("path");
		}else{
			//stop launch
			
			formatter.printHelp( "startCS", options );
			System.exit(1);
		}

		

	}

	public String getPath() {
		return path;
	}

	public String getIp() {
		return ip;
	}

	public boolean getHelp() {
		return help;
	}

}
