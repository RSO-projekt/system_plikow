package impl.server.master;

import java.util.ArrayList;

public class Configuration {
	public static int sRedundancy = 3;
	public static int sMinRedundancy = 2;
	public static int externalPort = 1205;
	public static int internalPort = 1206;
	public static ArrayList<String> sMainServerIPs;
	public static ArrayList<String> sDataServerIPs;
	public static int clientTimeout = 5000;
	public static int serverTimeout = 1000;
	static{
		sMainServerIPs = new ArrayList<String>();
		sDataServerIPs = new ArrayList<String>();
	}
	
}
