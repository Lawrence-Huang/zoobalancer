package idv.lawrence.zoobalancer;


public class ZooServerStatus {
	private String ipAddress;
	private int weight;
	public ZooServerStatus(){}
	public ZooServerStatus(String ipAddress,int weight){
		this.ipAddress = ipAddress;
		this.weight = weight;
	}
	public String getIpAddress(){
		return ipAddress;
	}
	public int getWeight(){
		return weight;
	}
	public void setIpAddress(String ipAddress){
		this.ipAddress = ipAddress;
	}
	public void setWeight(int weight){
		this.weight = weight;
	}
}

