package idv.lawrence.zoobalancer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;

/**
 * 
 * This ZooManager is the service layer of the ZooBalancer. It contains how to
 * initialize the Datatree in ZooKeeper, how to create persistent or ephemeral
 * znode, and how to choose the best server for forwarding client's request.
 *
 */
public class ZooManager {
	private String projectName;
	private String ipAddress;
	private String zkCluster;
	private CuratorFramework zk;
	private InterProcessMutex mutex;

	/**
	 * This constructor checks Datatree in the ZooKeeper whether have been
	 * created. If the Datatree doesn't be created, this constructor will create
	 * a parent znode which name is project name and have three children znodes
	 * which name are server, session, and lock.
	 * 
	 * @param projectName
	 * @param ipAddress
	 * @param zkCluster
	 * @throws Exception
	 * 
	 */
	public ZooManager(String projectName, String ipAddress, String zkCluster)
			throws Exception {
		this.projectName = projectName;
		this.ipAddress = ipAddress;
		this.zkCluster = zkCluster;
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);

		zk = CuratorFrameworkFactory.newClient(zkCluster, retryPolicy);
		zk.start();

		if (zk.checkExists().forPath("/" + projectName) == null) {
			zk.create().withMode(CreateMode.PERSISTENT)
					.forPath("/" + projectName);
		}
		if (zk.checkExists().forPath("/" + projectName + "/server") == null) {
			zk.create().withMode(CreateMode.PERSISTENT)
					.forPath("/" + projectName + "/server");
		}
		if (zk.checkExists().forPath("/" + projectName + "/session") == null) {
			zk.create().withMode(CreateMode.PERSISTENT)
					.forPath("/" + projectName + "/session");
		}
		if (zk.checkExists().forPath("/" + projectName + "/lock") == null) {
			zk.create().withMode(CreateMode.PERSISTENT)
					.forPath("/" + projectName + "/lock");
		}
		if (zk.checkExists().forPath("/" + projectName + "/lock/" + ipAddress) == null) {
			zk.create().withMode(CreateMode.PERSISTENT)
					.forPath("/" + projectName + "/lock/" + ipAddress);
		}

		zk.create()
				.withMode(CreateMode.EPHEMERAL)
				.forPath("/" + projectName + "/server/" + ipAddress,
						ByteBuffer.allocate(4).putInt(0).array());

		mutex = new InterProcessMutex(zk, "/" + projectName + "/lock/"
				+ ipAddress);
	}

	/**
	 * This method registers session Id which belongs one of the client to
	 * ZooKeeper.
	 * 
	 * @param sessionId
	 */
	public void registerSession(String sessionId) {
		String sessionRecordPath = "/" + projectName + "/session/" + sessionId;
		try {
			if (zk.checkExists().forPath(sessionRecordPath) == null)
				zk.create().withMode(CreateMode.PERSISTENT)
						.forPath(sessionRecordPath, ipAddress.getBytes());
			else
				zk.setData().forPath(sessionRecordPath, ipAddress.getBytes());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method unregisters session Id which belongs one of the client from
	 * ZooKeeper
	 * 
	 * @param sessionId
	 */
	public void unregisterSession(String sessionId) {
		String sessionRecordPath = "/" + projectName + "/session/" + sessionId;
		try {
			if (zk.checkExists().forPath(sessionRecordPath) != null)
				zk.delete().forPath(sessionRecordPath);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method updates that this session Id belong to this tomcat now.
	 * 
	 * @param sessionId
	 * @throws Exception
	 */
	public void updateSessionInfo(String sessionId) throws Exception {
		String sessionRecordPath = "/" + projectName + "/session/" + sessionId;
		if (zk.checkExists().forPath(sessionRecordPath) == null)
			zk.create().withMode(CreateMode.PERSISTENT)
					.forPath(sessionRecordPath, sessionId.getBytes());
		else
			zk.setData().forPath(sessionRecordPath, sessionId.getBytes());
	}

	/**
	 * This method looks for which the session Id belong to. And it returns the
	 * address of the Tomcat.
	 * 
	 * @param sessionId
	 * @return
	 * @throws Exception
	 */
	public String lookupSession(String sessionId) throws Exception {
		if (sessionId == null)
			throw new NullPointerException();
		else
			return new String(zk.getData().forPath(
					"/" + projectName + "/session/" + sessionId));
	}

	/**
	 * This method returns the current Tomcat loading.
	 * 
	 * @return
	 * @throws Exception
	 */
	public int getCurrentLoad() throws Exception {
		String serverMetricPath = "/" + projectName + "/server/" + ipAddress;
		return ByteBuffer.wrap(zk.getData().forPath(serverMetricPath)).getInt();
	}

	/**
	 * This method adds the current Tomcat loading. And it demonstrates the
	 * mutex which is provided by Apache Curator.
	 * 
	 * @param num
	 */
	public void increseLoad(int num) {
		String serverMetricPath = "/" + projectName + "/server/" + ipAddress;

		try {
			if (mutex.acquire(60, TimeUnit.SECONDS)) {
				int pre_weight = ByteBuffer.wrap(
						zk.getData().forPath(serverMetricPath)).getInt();
				zk.setData()
						.forPath(
								serverMetricPath,
								ByteBuffer.allocate(4).putInt(pre_weight + num)
										.array());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				mutex.release();
			} catch (Exception e) {
			}
		}

	}

	/**
	 * This method reduces the current Tomcat loading. And it demonstrates the
	 * mutex which is provided by Apache Curator.
	 * 
	 * @param num
	 */
	public void decreaseLoad(int num) {
		String serverMetricPath = "/" + projectName + "/server/" + ipAddress;

		try {
			if (mutex.acquire(60, TimeUnit.SECONDS)) {
				int pre_weight = ByteBuffer.wrap(
						zk.getData().forPath(serverMetricPath)).getInt();
				zk.setData()
						.forPath(
								serverMetricPath,
								ByteBuffer.allocate(4).putInt(pre_weight - num)
										.array());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				mutex.release();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * This method returns IP address of the lowest loading tomcat. First, it
	 * get all server lists in the ZooKeeper. After that, this lists are sorted
	 * by loading weight. As the result, it can get the best tomcat server which
	 * the clinet's request should be forwarded to.
	 * 
	 * @return
	 * @throws Exception
	 */
	public String getBestServer() throws Exception {

		String serverRecordPath = "/" + projectName + "/server";
		ArrayList<ZooServerStatus> serverList = new ArrayList<ZooServerStatus>();

		for (String ipAddress : zk.getChildren().forPath(serverRecordPath)) {
			int weight = ByteBuffer.wrap(
					zk.getData().forPath(serverRecordPath + "/" + ipAddress))
					.getInt();
			serverList.add(new ZooServerStatus(ipAddress, weight));
		}

		serverList.sort(new Comparator<ZooServerStatus>() {
			public int compare(ZooServerStatus s1, ZooServerStatus s2) {
				return s1.getWeight() - s2.getWeight();
			}
		});

		return serverList.get(0).getIpAddress();

	}
}
