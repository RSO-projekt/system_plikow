package impl.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;

import rso.at.ClientMasterService;
import rso.at.MasterMasterService;

import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Profile.Section;

public class ServerMain {

	private final static String PATH_TO_CONFIG_FILE = "config.ini";
	
	private static void readConfig(){
		Ini ini;
		try {
			ini = new Ini(new File(PATH_TO_CONFIG_FILE));
			Section section = ini.get("");
			Configuration.sRedundancy = Integer.valueOf(section.get("redundancy"));
			Configuration.sMinRedundancy = Integer.valueOf(section.get("min-redundancy"));
			Configuration.sInternalPort = Integer.valueOf(section.get("internal-port"));
			Configuration.sExternalPort = Integer.valueOf(section.get("external-port"));
			int num = Integer.valueOf(section.get("master-server-num"));
			Configuration.sMainServerIPs.clear();
			Configuration.sDataServerIPs.clear();
			for(int i =0; i < num; i++){
				Configuration.sMainServerIPs.add(section.get((new String("master-server")) + String.valueOf(i)));
			}
			num = Integer.valueOf(section.get("data-server-num"));
			for(int i =0; i < num; i++){
				Configuration.sDataServerIPs.add(section.get((new String("data-server")) + String.valueOf(i)));
			}
			
		} catch (InvalidFileFormatException e) {
			e.printStackTrace();
		} catch(FileNotFoundException e){
			System.out.println("Configuration file not found. Using default settings.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void start() {
		try {
			TServerSocket serverTransport = new TServerSocket(Configuration.sInternalPort);
			//TODO skasowac niepotrzeby port, lub podzielic na dwa Sever Sockety dla DataMaster i MasterMaster

			TMultiplexedProcessor processor = new TMultiplexedProcessor();
			
			processor.registerProcessor("ClientMaster", new ClientMasterService.Processor<ClientMasterImpl>(new ClientMasterImpl()));
			processor.registerProcessor("MasterMaster", new MasterMasterService.Processor<MasterMasterImpl>(new MasterMasterImpl()));
			

			TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).
					processor(processor));
			System.out.println("Starting server on port " + Configuration.sInternalPort +" ...");
			
			server.serve();
		} catch (TTransportException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		readConfig();
		ServerMain srv = new ServerMain();
		srv.start();
	}

}
