package impl.server.master;

import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.server.TThreadPoolServer.Args;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.transport.TTransportFactory;

import rso.at.MasterMasterService;

class UDPMasterMasterThread extends Thread {
	private FileSystemMonitor monitor;
	private int serverID;
	private TThreadPoolServer server;
	
	public UDPMasterMasterThread(FileSystemMonitor monitor, int serverID) throws TTransportException{
		this.monitor = monitor;
		this.serverID = serverID;
		TServerSocket serverTransportExternal = new TServerSocket(Configuration.internalPort);
		TTransportFactory factory = new TFramedTransport.Factory();
		
		TMultiplexedProcessor processor = new TMultiplexedProcessor();
		Args args = new TThreadPoolServer.Args(serverTransportExternal);
		args.processor(processor);
		args.transportFactory(factory);
		
		processor.registerProcessor("MasterMaster", new MasterMasterService.Processor<MasterMasterImpl>(new MasterMasterImpl(monitor)));
		server = new TThreadPoolServer(args);
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