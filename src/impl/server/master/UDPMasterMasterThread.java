package impl.server.master;

import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.transport.TTransportFactory;

import rso.at.MasterMasterService;

class UDPMasterMasterThread extends Thread {
	private FileSystemMonitor monitor;
	private int serverID;
	private TSimpleServer server;
	
	public UDPMasterMasterThread(FileSystemMonitor monitor, int serverID) throws TTransportException{
		this.monitor = monitor;
		this.serverID = serverID;
		int port = 1300 + serverID - 1;
		TServerSocket serverTransportExternal = new TServerSocket(port);
		TTransportFactory factory = new TFramedTransport.Factory();
		
		TMultiplexedProcessor processor = new TMultiplexedProcessor();
		TServer.Args args = new TServer.Args(serverTransportExternal);
		args.processor(processor);
		args.transportFactory(factory);
		
		processor.registerProcessor("MasterMaster", new MasterMasterService.Processor<MasterMasterImpl>(new MasterMasterImpl(monitor)));
		server = new TSimpleServer(args);
		monitor.log("Starting UDP server on port " + port + 
				    " with priority " + this.serverID + "...");
	}
	
	public void run(){
		// Start election on startup
		monitor.startElection();
		// Start serving
		server.serve();
	}
}