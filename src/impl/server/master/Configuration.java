package impl.server.master;

import java.util.ArrayList;

public class Configuration {
	public static int sRedundancy = 3;
	public static int sMinRedundancy = 2;
	public static int sPort = 1200;
	public static ArrayList<String> sMainServerIPs;
	public static ArrayList<String> sDataServerIPs;
	public static int sTimeout = 1000;
	static{
		sMainServerIPs = new ArrayList<String>();
		sMainServerIPs.add("192.168.11.11");
		sMainServerIPs.add("192.168.11.12");
		sDataServerIPs = new ArrayList<String>();
		sDataServerIPs.add("192.168.11.13");
		sDataServerIPs.add("192.168.11.14");
		sDataServerIPs.add("192.168.11.15");
	}
	
}
