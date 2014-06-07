package impl.server.master;

import impl.Configuration;

import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.server.TThreadPoolServer.Args;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.transport.TTransportFactory;

import rso.at.MasterMasterService;

class MasterMasterThread extends Thread {
	private FileSystemMonitor monitor;
	private TThreadPoolServer server;
	
	public MasterMasterThread(FileSystemMonitor monitor, int serverID) throws TTransportException{
		this.monitor = monitor;
		TServerSocket serverTransportExternal = new TServerSocket(Configuration.internalPort);
		TTransportFactory factory = new TFramedTransport.Factory();
		
		TMultiplexedProcessor processor = new TMultiplexedProcessor();
		Args args = new TThreadPoolServer.Args(serverTransportExternal);
		args.processor(processor);
		args.transportFactory(factory);
		
		processor.registerProcessor("MasterMaster", new MasterMasterService.Processor<MasterMasterImpl>(new MasterMasterImpl(this.monitor)));
		server = new TThreadPoolServer(args);
		this.monitor.log("Starting Master-Master Server on port " + Configuration.internalPort + 
				    " with priority " + serverID + "...");
	}
	
	public void run(){
		// Start election on startup
		monitor.startElection();
		// Start serving
		server.serve();
	}
}