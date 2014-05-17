package impl.server.master;

import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.server.TNonblockingServer.Args;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;
import org.apache.thrift.transport.TTransportException;

class UDPMasterMasterThread extends Thread {
	private FileSystemMonitor monitor;
	private int serverID;
	private TServer server;
	
	public UDPMasterMasterThread(FileSystemMonitor monitor, int serverID) throws TTransportException{
		this.monitor = monitor;
		this.serverID = serverID;
		TNonblockingServerTransport serverTransport = new TNonblockingServerSocket(Configuration.internalPort);
		Args args = new TNonblockingServer.Args(serverTransport);
		TMultiplexedProcessor processor = new TMultiplexedProcessor();
		args.processor(processor);
		server = new TNonblockingServer(args);
		monitor.log("Starting UDP server on port " + Configuration.internalPort + 
				    " with priority " + this.serverID + "...");
	}
	
	public void run(){
		// Start election on startup
		monitor.startElection();
		// Start serving
		server.serve();
	}
}