package impl.server.master;

import impl.server.master.FileSystemMonitor.Connection;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

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
	
	private static void readConfig(){
		Properties prop = new Properties();
		try {
			FileReader reader = new FileReader(PATH_TO_CONFIG_FILE);
			prop.load(reader);
			Configuration.sRedundancy = Integer.parseInt(prop.getProperty("redundancy"));
			Configuration.sMinRedundancy = Integer.parseInt(prop.getProperty("min-redundancy"));
			Configuration.sInternalPort = Integer.parseInt(prop.getProperty("internal-port"));
			Configuration.sExternalPort = Integer.parseInt(prop.getProperty("external-port"));
			int num = Integer.parseInt(prop.getProperty("master-server-num"));
			Configuration.sMainServerIPs.clear();
			Configuration.sDataServerIPs.clear();
			for(int i =0; i < num; i++){
				Configuration.sMainServerIPs.add(prop.getProperty(new String("master-server") + String.valueOf(i)));
			}
			num = Integer.parseInt(prop.getProperty("data-server-num"));
			for(int i =0; i < num; i++){
				Configuration.sDataServerIPs.add(prop.getProperty(new String("data-server") + String.valueOf(i)));
			}
		} catch(FileNotFoundException e){
			System.out.println("Configuration file not found. Using default settings.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void start() throws EntryNotFound, InvalidOperation {
		try {
			TServerSocket serverTransport = new TServerSocket(Configuration.sInternalPort);
			//TODO skasowac niepotrzeby port, lub podzielic na dwa Sever Sockety dla DataMaster i MasterMaster

			TMultiplexedProcessor processor = new TMultiplexedProcessor();
			
			FileSystemMonitor monitor = new FileSystemMonitor();
			String myIP = InetAddress.getLocalHost().getHostAddress();
			for (String ip : Configuration.sMainServerIPs) {
				if (!ip.equals(myIP)) {
					monitor.addMasterConnection(ip, Configuration.sInternalPort);
				}
			}
			
			processor.registerProcessor("ClientMaster", new ClientMasterService.Processor<ClientMasterImpl>(new ClientMasterImpl(monitor)));
			processor.registerProcessor("MasterMaster", new MasterMasterService.Processor<MasterMasterImpl>(new MasterMasterImpl(monitor)));
			

			TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).
					processor(processor));
			System.out.println("Starting server on port " + Configuration.sInternalPort +" ...");
			
			monitor.openConnections();
			server.serve();
			
		} catch (TTransportException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws EntryNotFound, InvalidOperation{
		readConfig();
		ServerMain srv = new ServerMain();
		srv.start();
	}

}
