package edu.temple.cis.jenergy.computespace;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

public class RecoveryManager {

	CuratorFramework queueClient;
	String zookeeperConnectionString = "";
	int zooSessionTimeoutMs = 10000;
	int zooConnectionTimeoutMs = 10000;

	public RecoveryManager() {

		try {
			ClassPathHacker.addFile(System.getProperty("user.dir") + "/conf");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		setzooConn();

		RetryPolicy retryPolicy = new ExponentialBackoffRetry(10, 3);

		queueClient = CuratorFrameworkFactory.newClient(
				zookeeperConnectionString, zooSessionTimeoutMs,
				zooConnectionTimeoutMs, retryPolicy);
		queueClient.start();

	}

	public RecoveryManager(CuratorFramework queueClient) {

		this.queueClient = queueClient;

	}

	private void setzooConn() {

		String port = "2181";
		String connString = "";

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(new File("conf/hosts")));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String line = null;
		ArrayList<String> list = new ArrayList<String>();
		try {
			while ((line = reader.readLine()) != null) {
				list.add(line);
				// only add one of the zookeepers for now.
				break;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// System.out.println(stringBuilder.toString());

		for (String s : list) {
			connString = connString.concat(s);
			connString = connString.concat(":" + port);

			if (!s.equals(list.get(list.size() - 1))) {
				connString = connString.concat(",");
			}
		}
		this.zookeeperConnectionString = connString;
		// System.out.println(this.zookeeperConnectionString);

	}

	public static void main(String args[]) {

		if (args == null | args.length == 0) {
			System.out
					.println("Usage: \n true (turn recovery on)|false(turn recovery off)");
			return;
		}

		boolean status = false;
		if (args[0].compareTo("true") == 0) {
			status = true;
		} else if (args[0].compareTo("false") == 0) {
			status = false;
		} else {
			System.out
					.println("Usage: \n true (turn recovery on)|false(turn recovery off)");
			return;
		}

		System.out.println("Setting the recovery status to " + status);
		RecoveryManager recov = new RecoveryManager();

		recov.setRecoveryStatus(status);

		System.out.println("Confirming recovery status..."
				+ recov.checkRecoveryStatus());

		recov.close();

	}

	private void close() {
		this.queueClient.close();
	}

	public void setRecoveryStatus(boolean status) {

		if (status == true) {
			try {
				if (queueClient.checkExists().forPath("/recoveryZnode") == null) {
					queueClient.create().forPath("/recoveryZnode");
					System.out.println("Set the recovery status to " + status);
				} else {
					System.out.println("Recovery Status already in state: "
							+ status);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				if (queueClient.checkExists().forPath("/recoveryZnode") != null) {
					queueClient.delete().forPath("/recoveryZnode");
					System.out.println("Set the recovery status to " + status);
				} else {
					System.out.println("Recovery Status already in state: "
							+ status);
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public boolean checkRecoveryStatus() {
		try {
			if (queueClient.checkExists().forPath("/recoveryZnode") != null) {
				return true;
			} else
				return false;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

}
