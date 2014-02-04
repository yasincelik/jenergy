package edu.temple.cis.jenergy.computespace;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.persistence.PersistenceException;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.mortbay.log.Log;

import com.google.common.collect.ImmutableMap;
import com.netflix.astyanax.AstyanaxContext;
import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.connectionpool.NodeDiscoveryType;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.connectionpool.impl.ConnectionPoolConfigurationImpl;
import com.netflix.astyanax.connectionpool.impl.CountingConnectionPoolMonitor;
import com.netflix.astyanax.entitystore.DefaultEntityManager;
import com.netflix.astyanax.entitystore.EntityManager;
import com.netflix.astyanax.impl.AstyanaxConfigurationImpl;
import com.netflix.astyanax.model.ColumnFamily;
import com.netflix.astyanax.model.ConsistencyLevel;
import com.netflix.astyanax.serializers.StringSerializer;
import com.netflix.astyanax.thrift.ThriftFamilyFactory;

public class ComputeSpace {

	protected ColumnFamily<String, String> TABLE;
	protected Map<String, AstyanaxContext<Keyspace>> contextList = new HashMap<String, AstyanaxContext<Keyspace>>();
	protected String REPLICATION_FACTOR = "3";
	protected String RING_SEEDS = "";
	protected String zookeeperConnectionString = "";
	protected CuratorFramework queueClient;
	
	protected ConsistencyLevel readConsistency = ConsistencyLevel.CL_QUORUM;
	protected ConsistencyLevel writeConsistency = ConsistencyLevel.CL_QUORUM;
	
	//protected ConsistencyLevel readConsistency = ConsistencyLevel.CL_ONE;
	//protected ConsistencyLevel writeConsistency = ConsistencyLevel.CL_ONE;
	
	
	protected Random randy = new Random();
	private int zooSessionTimeoutMs = 10000;
	private int zooConnectionTimeoutMs = 10000;
	private RecoveryManager recovAgent;
	private boolean recoveryStatus = false;

	
	//TODO: this should be configurable outside
	private final int chunkSize = 20;

	public ComputeSpace() {
		TABLE = new ColumnFamily<String, String>("DEFAULT", // Column Family
				StringSerializer.get(), // Key Serializer
				StringSerializer.get()); // Column Serializer
		try {
			ClassPathHacker.addFile(System.getProperty("user.dir") + "/conf");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		setRingSeeds();
		setzooConn();

		setConsistency();
		
		System.out.println(this.zookeeperConnectionString);
		System.out.println(this.RING_SEEDS);

		createCuratorClient();

		recovAgent = new RecoveryManager(queueClient);
		recoveryStatus = recovAgent.checkRecoveryStatus();

	}

	public boolean create(String CSName) {

		if (CSName == null)
			return false;

		AstyanaxContext<Keyspace> context = createContext(CSName);

		context.start();

		contextList.put(CSName, context);

		Keyspace keyspace = context.getClient();
		try {
			if (recoveryStatus == false)
				keyspace.dropKeyspace();

		} catch (ConnectionException e1) {
		}

		try {

			keyspace.createKeyspace(ImmutableMap
					.<String, Object> builder()
					.put("strategy_options",
							ImmutableMap
									.<String, Object> builder()
									.put("replication_factor",
											REPLICATION_FACTOR).build())
					.put("strategy_class", "SimpleStrategy").build());

		} catch (ConnectionException e1) {
			// TODO Auto-generated catch block

			e1.printStackTrace();
			if (recoveryStatus == false) {
				Log.info("Cannot find the data backplane: Did you start the daemons?");
				Log.info("Terminating the current thread.");

				System.exit(1);
				return false;
			} else
				return true;

		}

		try {
			keyspace.createColumnFamily(TABLE, null);
		} catch (ConnectionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return true;
	}

	public boolean eraseCS(String CSName) {

		if (CSName == null)
			return false;

		AstyanaxContext<Keyspace> context = getContext(CSName);

		Keyspace keyspace = context.getClient();
		try {
			if (recoveryStatus == false)
				keyspace.dropKeyspace();
		} catch (ConnectionException e1) {
		}

		return true;
	}

	public boolean open(String CSName) {
		if (CSName == null)
			return false;

		AstyanaxContext<Keyspace> context = createContext(CSName);
		context.start();
		contextList.put(CSName, context);

		createCuratorClient();

		return true;
	}

	public boolean close() throws InterruptedException {

		cleanAllZnodes();

		if (contextList.isEmpty())
			return false;

		for (AstyanaxContext<Keyspace> context : contextList.values()) {
			if (context != null) {
				try {
					context.getClient().dropKeyspace();
				} catch (ConnectionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				context.shutdown();
				break;
			}
		}

		queueClient.close();

		return true;
	}

	public boolean closeForTesting() throws InterruptedException {
		if (contextList.isEmpty())
			return false;

		for (AstyanaxContext<Keyspace> context : contextList.values()) {
			if (context != null) {
				context.shutdown();
				break;
			}
		}

		queueClient.close();

		return true;
	}

	// normal put
	public boolean put(String CSName, MatrixTuple tupleObj)
			throws InterruptedException {

		if (CSName == null || tupleObj == null)
			return false;
		AstyanaxContext<Keyspace> context = getContext(CSName);
		Keyspace keyspace = context.getClient();
		EntityManager<MatrixTuple, String> entityManager = new DefaultEntityManager.Builder<MatrixTuple, String>()
				.withEntityType(MatrixTuple.class).withKeyspace(keyspace)
				.withColumnFamily(TABLE).withConsistency(writeConsistency)
				.build();
		MatrixTuple tupleInfo = new MatrixTuple();
		while (true) {
			try {

				if (tupleObj.data.length <= chunkSize) {
					entityManager.put(tupleObj);
				} else {
					int numRows = tupleObj.numRows;

					// ----------------step 1
					// figure out how many chunks to cut the tuple into
					int numSubTuples = getNumSubTuples(chunkSize, numRows);

					// ----------------step1.5
					// put the subTuples in the compute space
					putSubTuples(numSubTuples, chunkSize, tupleObj, CSName);

					// step 2
					// put an info tuple
					tupleInfo.id = tupleObj.id + "_info";
					tupleInfo.numRows = tupleObj.numRows;
					tupleInfo.numCols = tupleObj.numCols;

					entityManager.put(tupleInfo);
				}

				break;
			} catch (PersistenceException e) {
				Log.info("problem putting tuple:" + tupleObj.id);
				e.printStackTrace();
				TimeUnit.SECONDS.sleep(1);
			}
		}
		//Log.info("put: "+tupleObj.id);
		return true;
	}

	// chunked put
	public boolean put(String CSName, MatrixTuple tupleObj, int G)
			throws InterruptedException {

		if (CSName == null || CSName.compareTo("") == 0 || tupleObj == null
				|| G == 0)
			return false;

		int numRows = tupleObj.numRows;

		// ----------------step 1
		// figure out how many chunks to cut the tuple into
		int numSubTuples = getNumSubTuples(G, numRows);

		// ----------------step1.5
		// put the subTuples in the compute space
		putSubTuples(numSubTuples, G, tupleObj, CSName);

		// ----------------step2
		// clean up the sync,staged and finished znodes
		HashMap<String, String> CSqueues = new HashMap<String, String>();
		CSqueues.put("init", CSName + "_init");
		CSqueues.put("staged", CSName + "_staged");
		CSqueues.put("done", CSName + "_done");

		if (recoveryStatus == false) {
			// clean up queues znodes
			for (String s : CSqueues.keySet())
				cleanZnode(CSqueues.get(s));

			// ----------------step3
			// create the queues znodes
			for (String s : CSqueues.keySet())
				createZnodes(CSqueues.get(s));

			// ---------------step4
			// create a znode for each subTuple
			// append the subTuple number to it and store it in the init queue.
			createSubZnodes(CSqueues.get("init"), numSubTuples, tupleObj);
		} else {

			// RECOVERY MODE
			// if the queues already exist this will fail safely;

			// ----------------step3
			// create the queues znodes
			for (String s : CSqueues.keySet()) {
				createZnodes(CSqueues.get(s));
			}

			// RECOVERY MODE
			//
			// ---------------step4
			// create a znode for each subTuple
			// append the subTuple number to it and store it in the init queue.
			createSubZnodesRecovery(CSqueues.get("init"), CSqueues.get("done"),
					numSubTuples, tupleObj);

		}

		return true;
	}

	// read offset object
	public MatrixTuple read(String CSName, String TupleName, int offset)
			throws InterruptedException {

		if (CSName == null || TupleName == null)
			return null;

		AstyanaxContext<Keyspace> context = getContext(CSName);
		Keyspace keyspace = context.getClient();
		EntityManager<MatrixTuple, String> entityManager = new DefaultEntityManager.Builder<MatrixTuple, String>()
				.withEntityType(MatrixTuple.class).withKeyspace(keyspace)
				.withColumnFamily(TABLE).withConsistency(readConsistency)
				.build();
		MatrixTuple t;
		while (true) {
			try {
				//t = entityManager.get(TupleName + "_" + offset);
				
				
				t = read(CSName, TupleName+"_"+offset);
				
				if (t != null)
					return t;
			} catch (PersistenceException e) {
				// e.printStackTrace();
				//Log.info("READ: " + TupleName + "_" + offset
				//		+ " not yet available.");
				TimeUnit.SECONDS.sleep(1);
			}
		}
	}

	
	
	// read full object
	public MatrixTuple read(String CSName, String TupleName)
			throws InterruptedException {

		AstyanaxContext<Keyspace> context = getContext(CSName);
		Keyspace keyspace = context.getClient();

		EntityManager<MatrixTuple, String> entityManager = new DefaultEntityManager.Builder<MatrixTuple, String>()
				.withEntityType(MatrixTuple.class).withKeyspace(keyspace)
				.withColumnFamily(TABLE).withConsistency(readConsistency)
				.build();

		MatrixTuple t;
		while (true) {
			try {
				// try first to get the tuple info if any
				t = entityManager.get(TupleName + "_info");
				
				if (t != null) {
					//this means that we are reading a chunked full object

					int numRows = t.numRows;

					// ----------------step 1
					// figure out how many chunks the tuple was cut
					int numSubTuples = getNumSubTuples(chunkSize, numRows);

					// ----------------step1.5
					// get the tuple from the chunks
					t = getTuplefromChunks(numSubTuples, chunkSize, t, CSName,TupleName);
					if (t != null)
						return t;
					
				}

				else {
					//if there is no meta data then get the regular way
					t = entityManager.get(TupleName);
					if (t != null)
						return t;
				}
			} catch (PersistenceException e) {
				//either there was a problem with the connection or we are reading a smaller object
				//second level try catch
				try{
					t = entityManager.get(TupleName);
					if (t != null)
						return t;
				}catch (PersistenceException e2) {
		//			Log.info("Read waiting for: " + TupleName);
					TimeUnit.SECONDS.sleep(1);
					continue;
				}
				
				
	//			Log.info("Read waiting for: " + TupleName);
				TimeUnit.SECONDS.sleep(1);
				// e.printStackTrace();

			}
		}
	}
	
	
	
	public MatrixTuple get(String CSName, String TupleName)
			throws InterruptedException {
		while (true) {

			String selectedTuple = null;

			boolean phase1 = false;

			// selectedTuple = getNextTupleFromQueue("init", CSName, TupleName);

			selectedTuple = getNextTupleFromQueue("init", CSName, TupleName);
			if (selectedTuple != null) {
				phase1 = true;
			} else {
				selectedTuple = getNextTupleFromQueue("staged", CSName,
						TupleName);
				if (selectedTuple != null)
					phase1 = true;
			//	else
			//		return null;
			}

			if (phase1 == true) {
				// step 2: directread and build the return tuple
				return read(CSName, selectedTuple);
			}// else keep trying to read a task from the zooQueue.
		}

	}

	private String getNextTupleFromQueue(String queue, String CSName,
			String TupleName) throws InterruptedException {

		// step 1: get a tuple from the tuple list on the distributed
		// queue mechanism

		String CSNameSync;
		String selectedTuple = null;

		if (queue.compareTo("init") == 0) {
			CSNameSync = getInitQueueName(CSName);
		} else if (queue.compareTo("staged") == 0) {
			CSNameSync = getStagedQueueName(CSName);
		} else if (queue.compareTo("done") == 0) {
			CSNameSync = getStagedQueueName(CSName);
		} else
			return null;

		InterProcessMutex lock = new InterProcessMutex(queueClient, CSNameSync);

		try {
			// lock the init queue
			// Log.info("Thread: " + Thread.currentThread().getName()
			// + " waiting for lock:" + lock.toString());

			lock.acquire();
			// if (lock.acquire(1, TimeUnit.SECONDS)) {

			// get the list of tasks available
			List<String> listOfTasks;

			listOfTasks = queueClient.getChildren().forPath(CSNameSync);

			// remove the lock strings from the list
			listOfTasks = filterLocks(listOfTasks);

			if (listOfTasks.size() != 0) {
				// if there tasks left
				// get the first one on the list
				// this does not guarantee any order
				Collections.sort(listOfTasks);
				selectedTuple = listOfTasks.get(0);

				// get the tuple name
				String tupleFromZoo = selectedTuple.split("_")[0];
				// check if it matches the tupleName
				// this is a quick fix
				if (!tupleFromZoo.equalsIgnoreCase(TupleName)) {
					return null;
				} else {
					String initPath = getInitQueueName(CSName) + "/"
							+ selectedTuple;
					String stagedPath = getStagedQueueName(CSName) + "/"
							+ selectedTuple;
					String donePath = getDoneQueueName(CSName) + "/"
							+ selectedTuple;

					// mv init->staged
					if (queue.compareTo("init") == 0) {
						if (queueClient.checkExists().forPath(stagedPath) == null) {
							queueClient.create().forPath(stagedPath);
						}
						queueClient.delete().forPath(initPath);

					}// mv staged->done
					else if (queue.compareTo("staged") == 0) {
						Log.info("Recomputing from staged queue: "
								+ selectedTuple);
						if (queueClient.checkExists().forPath(donePath) == null) {
							queueClient.create().forPath(donePath);
						}
						queueClient.delete().forPath(stagedPath);
					}

				}
			}

		} catch (Exception e) {

			if (Thread.interrupted()) {
				Log.info(Thread.currentThread().getName().toString()
						+ " was terminated.");
				throw new InterruptedException();
			} else
				e.printStackTrace();

		} finally {
			try {
				lock.release();
			} catch (Exception e) {
				if (Thread.interrupted()) {
					Log.info(Thread.currentThread().getName().toString()
							+ " was terminated.");
					throw new InterruptedException();
				} else {
					// e.printStackTrace();
					// Log.info("Problem with releasing the lock");
				}
				// System.exit(1);
			}
		}

		return selectedTuple;

	}

	protected AstyanaxContext<Keyspace> getContext(String ks) {
		return contextList.get(ks);
	}

	protected void createCuratorClient() {
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(10, 3);

		queueClient = CuratorFrameworkFactory.newClient(
				this.zookeeperConnectionString, zooSessionTimeoutMs,
				zooConnectionTimeoutMs, retryPolicy);
		queueClient.start();
	}

	protected AstyanaxContext<Keyspace> createContext(String CSName) {

		AstyanaxContext<Keyspace> context = new AstyanaxContext.Builder()
				.forCluster("Meloui")
				.forKeyspace(CSName)
				.withAstyanaxConfiguration(
						new AstyanaxConfigurationImpl()
								.setDiscoveryType(
										NodeDiscoveryType.RING_DESCRIBE)
								.setCqlVersion("3.0.0")
								.setTargetCassandraVersion("1.2"))
				.withConnectionPoolConfiguration(
						new ConnectionPoolConfigurationImpl(
								"ComputeSpaceConnectionPool").setPort(9160)
								.setMaxConnsPerHost(1)
								.setSeeds(this.RING_SEEDS))
				// this has to be configurable outside
				.withConnectionPoolMonitor(new CountingConnectionPoolMonitor())
				.buildKeyspace(ThriftFamilyFactory.getInstance());

		return context;
	}

	private void cleanZnode(String CSNameSync) throws InterruptedException {

		try {
			if (queueClient.checkExists().forPath("/" + CSNameSync) != null) {
				for (String s : queueClient.getChildren().forPath(
						"/" + CSNameSync)) {
					queueClient.delete().forPath("/" + CSNameSync + "/" + s);
				}
				queueClient.delete().forPath("/" + CSNameSync);
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			// e1.printStackTrace();
			if (Thread.interrupted()) {
				Log.info(Thread.currentThread().getName().toString()
						+ " was terminated.");
				throw new InterruptedException();
			} else {
				e1.printStackTrace();
				Log.info("Problem Cleaning the znode:" + CSNameSync);
			}
		}
	}

	private void cleanNonLockZnode(String CSNameSync) {

		try {
			if (queueClient.checkExists().forPath("/" + CSNameSync) != null) {
				List<String> paths = queueClient.getChildren().forPath(
						"/" + CSNameSync);

				paths = filterLocks(paths);

				for (String s : paths) {
					// Log.info("Removing: " + "/" + CSNameSync + "/" + s);
					queueClient.delete().forPath("/" + CSNameSync + "/" + s);
				}

				// queueClient.delete().forPath("/" + CSNameSync);
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			// e1.printStackTrace();
			Log.info("Problem Cleaning the znode:" + CSNameSync);
		}
	}

	private void cleanAllZnodes() throws InterruptedException {
		// this only cleans the subtuples ids but not the lock znodes for
		// simplicity
		// the workers will see that there is no more work and terminate.

		InterProcessMutex lock = null;

		// get the list of queues

		List<String> paths = null;

		try {
			paths = queueClient.getChildren().forPath("/");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			Log.info("Could not get the list of queues");
		}

		paths = filterZKPaths(paths);

		do {
			Log.info("Attempting to clean the queues");
			for (String s : paths) {
				try {
					Log.info("Trying to lock queue for cleaning: " + s);
					// try to lock a queue
					lock = new InterProcessMutex(queueClient, "/" + s);
					lock.acquire();
					// if (lock.acquire(1,TimeUnit.NANOSECONDS)) {
					// if successful remove all znodes that are not
					// locking znodes.
					Log.info("Cleaning Queue: " + s);
					cleanNonLockZnode(s);
					// }
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					try {
						lock.release();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		} while (!checkIfEmptyQueues());

	}

	private boolean checkIfEmptyQueues() throws InterruptedException {

		List<String> paths;
		try {
			paths = queueClient.getChildren().forPath("/");

			paths = filterZKPaths(paths);

			List<String> innerPaths;
			for (String s : paths) {
				innerPaths = queueClient.getChildren().forPath("/" + s);
				innerPaths = filterLocks(innerPaths);
				if (innerPaths.size() != 0)
					return false;
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			if (Thread.interrupted()) {
				Log.info(Thread.currentThread().getName().toString()
						+ " was terminated.");
				throw new InterruptedException();
			} else
				e.printStackTrace();
		}
		return true;
	}

	private int getNumSubTuples(int G, int numRows) {
		if (G == 0)
			return 0;

		int numSubTuples = 0;
		if (G > numRows) {
			G = numRows;
			numSubTuples = 1;
		} else {

			numSubTuples = numRows / G;
			if (numRows % G != 0) {
				numSubTuples++;
			}
		}
		return numSubTuples;
	}

	private void putSubTuples(int numSubTuples, int G, MatrixTuple tupleObj,
			String CSName) throws InterruptedException {

		List<MatrixTuple> list = getSubTuples(numSubTuples, G, tupleObj);

		for (MatrixTuple t : list) {
			//Log.info("put: " + t.id);
			put(CSName, t);
		}

	}
	
	private MatrixTuple getTuplefromChunks(int numSubTuples, int chunkSize, MatrixTuple tupleInfo,
			String CSName,String tupleName) throws InterruptedException {

		MatrixTuple t = new MatrixTuple();
		
		double[][] fullMatrix = new double[tupleInfo.numRows][tupleInfo.numRows];
		
		for (int i = 0; i < numSubTuples; i++) {
			
			t = read(CSName, tupleName, i);
			//Log.info("got: "+t.id);
			for (int j = 0; j < t.numRows; j++) {
				fullMatrix[t.startRow + j] = t.data[j];
			}
			
		}
		
		t = new MatrixTuple(tupleName, fullMatrix);
		
		return t;

	}

	private List<MatrixTuple> getSubTuples(int numSubTuples, int G,
			MatrixTuple tupleObj) {

		List<MatrixTuple> list = new ArrayList<MatrixTuple>(numSubTuples);
		double[][] matrix = tupleObj.data;

		int lastChunkSize = matrix.length % G;
		int matrixRow = 0;
		double[][] submatrix;

		for (int i = 0; i < numSubTuples; i++) {
			// if we did not reach the last chunk
			if (i < numSubTuples - 1) {
				submatrix = new double[G][];
				for (int j = 0; j < G; j++) {
					submatrix[j] = matrix[matrixRow];
					matrixRow++;
				}
			} else {
				// last chunk
				if (lastChunkSize == 0 && matrix.length != 0)
					lastChunkSize = G;

				submatrix = new double[lastChunkSize][];
				for (int j = 0; j < lastChunkSize; j++) {
					submatrix[j] = matrix[matrixRow];
					matrixRow++;
				}
			}
			list.add(new MatrixTuple(tupleObj.id + "_" + i, submatrix, i * G));
		}
		return list;
	}

	private List<String> filterLocks(List<String> listOfTasks) {
		List<String> newList = new ArrayList<String>(listOfTasks.size());

		for (String s : listOfTasks) {
			if (s.contains("lock")) {
				// System.out.println(s);
			} else {
				newList.add(s);
			}
		}
		return newList;
	}

	private List<String> filterZKPaths(List<String> listOfTasks) {
		List<String> newList = new ArrayList<String>(listOfTasks.size());

		for (String s : listOfTasks) {
			if (s.contains("zookeeper")) {
				// System.out.println(s);
			} else {
				newList.add(s);
			}
		}
		return newList;
	}

	private void setzooConn() {

		int port = 2181;
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
				//break;
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
			//ugly hack here to get the port of the next zookeeper
			
			port = port +1 ;
		}
		this.zookeeperConnectionString = connString;
		System.out.println(this.zookeeperConnectionString);

	}

	private void setRingSeeds() {
		// TODO Auto-generated method stub
		String port = "9160";
		String RING_SEEDS = "";

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
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String s : list) {
			RING_SEEDS = RING_SEEDS.concat(s);
			RING_SEEDS = RING_SEEDS.concat(":" + port);

			if (!s.equals(list.get(list.size() - 1))) {
				RING_SEEDS = RING_SEEDS.concat(",");
			}
		}

		this.RING_SEEDS = RING_SEEDS;

		// System.out.println(RING_SEEDS);
	}
	private void setConsistency() {

		readConsistency = ConsistencyLevel.CL_QUORUM;
		writeConsistency = ConsistencyLevel.CL_QUORUM;

		BufferedReader reader = null;
		
		try {
			reader = new BufferedReader(new FileReader(new File("conf/consistency.conf")));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String line = null;
		ArrayList<String> list = new ArrayList<String>();
		try {
			while ((line = reader.readLine()) != null) {
				list.add(line);
				// only read the first line
				break;
			}
			
			if (list.get(0).toString().equalsIgnoreCase("CL_ONE")){
				readConsistency = ConsistencyLevel.CL_ONE;
				writeConsistency = ConsistencyLevel.CL_ONE;
				//System.out.println("Consistency Level found:"+ list.get(0).toString()+"\n");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Log.info("Read consistency: "+ readConsistency.name().toString());
		Log.info("Write consistency: "+ writeConsistency.name().toString());

	}

	private void createZnodes(String CSNameSync) {
		// create the sync znodes
		try {
			queueClient.create().forPath("/" + CSNameSync);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	private void createSubZnodes(String CSNameSync, int numSubTuples,
			MatrixTuple tupleObj) {
		for (int i = 0; i < numSubTuples; i++) {
			try {
				queueClient.create().forPath(
						"/" + CSNameSync + "/" + tupleObj.id + "_" + i);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void createSubZnodesRecovery(String CSNameSync, String CSNameDone,
			int numSubTuples, MatrixTuple tupleObj) {

		boolean subTupleExists;

		for (int i = 0; i < numSubTuples; i++) {
			try {
				// only write the subtuple in the init queue if the tuple is not
				// done
				if (queueClient.checkExists().forPath(
						"/" + CSNameDone + "/" + tupleObj.id + "_" + i) == null) {
					queueClient.create().forPath(
							"/" + CSNameSync + "/" + tupleObj.id + "_" + i);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private String getInitQueueName(String CSName) {
		return "/" + CSName + "_" + "init";
	}

	private String getStagedQueueName(String CSName) {
		return "/" + CSName + "_" + "staged";
	}

	private String getDoneQueueName(String CSName) {
		return "/" + CSName + "_" + "done";
	}
}
