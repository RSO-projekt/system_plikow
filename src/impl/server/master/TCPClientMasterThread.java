package impl.server.master;

import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;

import rso.at.ClientMasterService;

public class TCPClientMasterThread extends Thread {
	private FileSystemMonitor monitor;
	TServer server;

	public TCPClientMasterThread(FileSystemMonitor monitor, int serverID) throws TTransportException{
		this.monitor = monitor;
		
		// Prepare socket for connection
		TServerSocket serverTransportInternal = new TServerSocket(Configuration.externalPort);
		TMultiplexedProcessor processor = new TMultiplexedProcessor();
		processor.registerProcessor("ClientMaster", new ClientMasterService.Processor<ClientMasterImpl>(new ClientMasterImpl(monitor)));
		server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransportInternal).
				processor(processor));
		monitor.log("Starting TCP server on port " + Configuration.externalPort + 
				    " with priority " + serverID + "...");
	}
	
	public void run(){
		server.serve();
	}

}
