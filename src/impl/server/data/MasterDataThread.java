package impl.server.data;

import impl.server.master.Configuration;
import impl.server.master.FileSystemMonitor;
import impl.server.master.MasterMasterImpl;

import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.server.TThreadPoolServer.Args;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.transport.TTransportFactory;

import rso.at.MasterDataService;

public class MasterDataThread extends Thread{
private TThreadPoolServer server;
	
	public MasterDataThread(int serverID) throws TTransportException{
		TServerSocket serverTransportExternal = new TServerSocket(Configuration.internalPort);
		TTransportFactory factory = new TFramedTransport.Factory();
		
		TMultiplexedProcessor processor = new TMultiplexedProcessor();
		Args args = new TThreadPoolServer.Args(serverTransportExternal);
		args.processor(processor);
		args.transportFactory(factory);
		
		processor.registerProcessor("MasterData", new MasterDataService.Processor<MasterDataImpl>(new MasterDataImpl()));
		server = new TThreadPoolServer(args);
	}
	
	public void run(){
		// Start serving
		server.serve();
	}

}
