package idv.lawrence.zoobalancer;

/**
 * This is a POJO structure. It records the IP Address of the Tomcat and current
 * loading status of the Tomcat.
 * 
 *
 */
public class ZooServerStatus {
	private String ipAddress;
	private int weight;

	public ZooServerStatus() {
	}

	public ZooServerStatus(String ipAddress, int weight) {
		this.ipAddress = ipAddress;
		this.weight = weight;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public int getWeight() {
		return weight;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}
}
