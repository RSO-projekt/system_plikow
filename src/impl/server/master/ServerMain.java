package impl.server.master;

import java.io.FileReader;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.TreeSet;

import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;

import rso.at.ClientMasterService;
import rso.at.EntryNotFound;
import rso.at.InvalidOperation;
import rso.at.MasterMasterService;

public class ServerMain {

	private final static String PATH_TO_CONFIG_FILE = "properties.conf";
	
	private static void readConfig() throws InvalidOperation, IOException {
		Properties prop = new Properties();
		FileReader reader = new FileReader(PATH_TO_CONFIG_FILE);
		prop.load(reader);
		
		String redundancy = prop.getProperty("redundancy");
		if (redundancy == null) {
			throw new InvalidOperation(200, "\"redundancy\" (int) key expected in configuration file");
		}
		Configuration.sRedundancy = Integer.parseInt(redundancy);
		
		String minRedundancy = prop.getProperty("min-redundancy");
		if (minRedundancy == null) {
			throw new InvalidOperation(201, "\"min-redundancy\" (int) key expected in configuration file");
		}
		Configuration.sMinRedundancy = Integer.parseInt(minRedundancy);
		
		String port = prop.getProperty("port");
		if (port == null) {
			throw new InvalidOperation(202, "\"port\" (int) key expected in configuration file");
		}
		Configuration.sPort = Integer.parseInt(port);
		
		String timeout = prop.getProperty("timeout");
		if (timeout == null) {
			throw new InvalidOperation(202, "\"timeout\" (int) key expected in configuration file");
		}
		Configuration.sTimeout = Integer.parseInt(timeout);
		
		String masterServerNum = prop.getProperty("master-server-num");
		if (masterServerNum == null) {
			throw new InvalidOperation(203, "\"master-server-num\" (int) key expected in configuration file");
		}
		int num = Integer.parseInt(masterServerNum);
		
		Configuration.sMainServerIPs.clear();
		Configuration.sDataServerIPs.clear();
		for(int i =0; i < num; i++){
			String key = new String("master-server") + String.valueOf(i);
			String mainServerIP = prop.getProperty(key);
			if (mainServerIP != null) {
				Configuration.sMainServerIPs.add(mainServerIP);	
			}
		}
		num = Integer.parseInt(prop.getProperty("data-server-num"));
		for(int i =0; i < num; i++){
			String key = new String("data-server") + String.valueOf(i);
			String dataServerIP = prop.getProperty(key);
			if (dataServerIP != null) {
				Configuration.sDataServerIPs.add(dataServerIP);
			}
		}
	}
	
	private void start() throws EntryNotFound, InvalidOperation, 
	                            TTransportException, SocketException {
		// Prepare socket for connection
		TServerSocket serverTranexternalPort = new TServerSocket(Configuration.sPort);
		TMultiplexedProcessor processor = new TMultiplexedProcessor();
		
		// Find all available IPs and save them in map.
		TreeSet<String> myIPs = new TreeSet<String>();
		Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
		for (NetworkInterface ni : Collections.list(nets)) {
			Enumeration<InetAddress> addrs = ni.getInetAddresses();
			for (InetAddress ia : Collections.list(addrs)) {
				if (ia instanceof Inet4Address) {
					Inet4Address ia4 = (Inet4Address)ia;
					myIPs.add(ia4.getHostAddress());
				}
			}
		}
		
		// Prepare file system monitor and find other's servers IP.
		FileSystemMonitor monitor = new FileSystemMonitor();
		boolean foundLocalIP = false;
		int serverID = 0;
		int myServerID = 0;
		for (String ip : Configuration.sMainServerIPs) {
			serverID++;
			if (!myIPs.contains(ip)) {
				monitor.addMasterConnection(ip, Configuration.sPort, serverID);
			} else {
				if (foundLocalIP) {
					throw new InvalidOperation(100, "Multiple local IP's in configuration file are not allowed");
				} else {
					foundLocalIP = true;
					myServerID = serverID;
					monitor.setServerID(myServerID);
				}
			}
		}
		
		// If not found local IP, abort.
		if (!foundLocalIP) {
			throw new InvalidOperation(101, "Cannot find local IP number in configuration file");
		}
		
		// Register services
		processor.registerProcessor("ClientMaster", new ClientMasterService.Processor<ClientMasterImpl>(new ClientMasterImpl(monitor)));
		processor.registerProcessor("MasterMaster", new MasterMasterService.Processor<MasterMasterImpl>(new MasterMasterImpl(monitor)));
		
		// Create threaded server
		TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTranexternalPort).
				processor(processor));
		monitor.log("Starting server on port " + Configuration.sPort + 
				    " with priority " + myServerID + "...");
		
		// Start election on startup
		monitor.startElection();
		
		// Start serving
		server.serve();
		
//		TCPClientMasterThread tcpClientConn = new TCPClientMasterThread(monitor, myServerID);
//		tcpClientConn.start();
//		
//		UDPMasterMasterThread udpMasterConn = new UDPMasterMasterThread(monitor, myServerID);
//		udpMasterConn.start();
	}

	public static void main(String[] args) {
		ServerMain srv = new ServerMain();
		try {
			readConfig();
			srv.start();
		} catch (EntryNotFound e) {
			System.out.println("Error(" + e.code + "): " + e.message);
		} catch (InvalidOperation e) {
			System.out.println("Error(" + e.code + "): " + e.message);
		} catch (TTransportException e) {
			System.out.println("Error(TTranexternalPort):");
			e.printStackTrace();
		} catch (SocketException e) {
			System.out.println("Error(Socket):");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Error(IO): Configuration file not found or bad syntax");
		}
	}

}
