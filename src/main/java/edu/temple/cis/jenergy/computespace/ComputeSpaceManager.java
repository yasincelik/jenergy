package edu.temple.cis.jenergy.computespace;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.cassandra.service.CassandraDaemon;
import org.apache.cassandra.thrift.Cassandra.system_add_column_family_args;
import org.mortbay.log.Log;
import org.yaml.snakeyaml.Yaml;

public class ComputeSpaceManager implements Runnable {

	private String[] runArgs;

	public ComputeSpaceManager(String[] args) {
		this.runArgs = args;
	}

	public void run() {
		setAndStart(this.runArgs);
	}

	public static void main(String[] args) {
		setAndStart(args);
	}

	private static void setAndStart(String[] args) {

		CSConfiguration csArgs = new CSConfiguration(args);

		// copy the configuration directory to the path specified.
		copyConfDir(csArgs.getPath());

		// modify the ip address in the conf file
		configConfig(csArgs);
		configLogConfig(csArgs);

		// modify the classpath to add the right conf directory
		try {
			if (System.getProperty("user.dir").contains("bin")) {
				ClassPathHacker.addFile(System.getProperty("user.dir")
						+ "/data/" + csArgs.getPath() + "/conf");
			} else {
				ClassPathHacker.addFile(System.getProperty("user.dir")
						+ "/bin/data/" + csArgs.getPath() + "/conf");
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Log.info(System.getProperty("user.dir"));

		String[] args2 = { "" };
		CassandraDaemon.main(args2);
	}

	private static void configLogConfig(CSConfiguration csArgs) {

		String curDir = System.getProperty("user.dir");
		// ugly hack here
		if (!System.getProperty("user.dir").contains("bin")) {
			curDir = curDir + "/bin";
		}
		String appPath = curDir + "/data/" + csArgs.getPath();
		File appLog = new File(appPath + "/system.log");

		// File confFile = new File(appPath + "/conf/log4j.properties");

		Properties defaultProps = new Properties();
		FileInputStream in = null;
		try {
			in = new FileInputStream(appPath + "/conf/log4j.properties");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			// load existing conf
			defaultProps.load(in);
			in.close();
			defaultProps.setProperty("log4j.appender.R.File", appLog.getPath());
			defaultProps.store(new FileOutputStream(appPath
					+ "/conf/log4j.properties"), "");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@SuppressWarnings("unchecked")
	private static void configConfig(CSConfiguration csArgs) {

		String curDir = System.getProperty("user.dir");

		// ugly hack here
		if (!System.getProperty("user.dir").contains("bin")) {
			curDir = curDir + "/bin";
		}

		String appPath = curDir + "/data/" + csArgs.getPath();
		File appData = new File(appPath + "/data");
		File appCommitLog = new File(appPath + "/commitlog");
		File appCaches = new File(appPath + "/saved_caches");

		File confFile = new File(appPath + "/conf/cassandra.yaml");

		InputStream input = null;
		try {
			input = new FileInputStream(confFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Yaml yaml = new Yaml();

		Map<String, List<String>> map = (Map<String, List<String>>) yaml
				.load(input);

		ArrayList<String> storagePaths = new ArrayList<String>();
		ArrayList<String> commitPaths = new ArrayList<String>();
		ArrayList<String> cachePaths = new ArrayList<String>();

		storagePaths.add(appData.getAbsolutePath());
		commitPaths.add(appCommitLog.getAbsolutePath());
		cachePaths.add(appCaches.getAbsolutePath());

		map.put("data_file_directories", storagePaths);
		map.put("commitlog_directory", commitPaths);
		map.put("saved_caches_directory", cachePaths);

		String ip = csArgs.getIp();
		ArrayList<String> listenIP = new ArrayList<String>();
		listenIP.add(ip);
		ArrayList<String> rpcIP = new ArrayList<String>();
		rpcIP.add(ip);

		map.put("listen_address", listenIP);
		map.put("rpc_address", rpcIP);

		// System.out.println(map);
		String output = yaml.dump(map);
		// System.out.println(output);

		try {
			PrintWriter out = new PrintWriter(confFile.getAbsoluteFile());
			out.println(output);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void copyConfDir(String path) {
		String curDir = System.getProperty("user.dir");
		// System.out.println(curDir);
		// ugly hack here
		if (!System.getProperty("user.dir").contains("bin")) {
			curDir = curDir + "/bin";
		}
		File srcFolder = new File(curDir + "/conf");

		File destFolder = new File(curDir + "/data/" + path + "/conf");

		// make sure source exists
		if (!srcFolder.exists()) {
			System.out.println("Conf Directory does not exist.");
			// just exit
			System.exit(0);
		} else {

			try {
				copyFolder(srcFolder, destFolder);
			} catch (IOException e) {
				e.printStackTrace();
				// error, just exit
				System.exit(0);
			}
		}

		// System.out.println("Done");
	}

	public static void copyFolder(File src, File dest) throws IOException {
		// this could fail unless the conf file is in the bin folder
		// either from a previous startCS --format or --initramfs
		if (src.isDirectory()) {

			// if directory not exists, create it
			if (!dest.exists()) {
				boolean status = dest.mkdirs();

				if (!status) {
					System.out.println("Not able to create data directory:"
							+ dest.getAbsolutePath());
					System.exit(1);
				}

				// System.out.println("Directory copied from " + src + "  to "
				// + dest);
			}

			// list all the directory contents
			String files[] = src.list();

			for (String file : files) {
				// construct the src and dest file structure
				File srcFile = new File(src, file);
				File destFile = new File(dest, file);
				// recursive copy
				copyFolder(srcFile, destFile);
			}

		} else {
			// if file, then copy it
			// Use bytes stream to support all file types
			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dest);

			byte[] buffer = new byte[1024];

			int length;
			// copy the file content in bytes
			while ((length = in.read(buffer)) > 0) {
				out.write(buffer, 0, length);
			}

			in.close();
			out.close();
			// System.out.println("File copied from " + src + " to " + dest);
		}
	}

}
