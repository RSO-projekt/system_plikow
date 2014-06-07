package impl.server.data;

import impl.Configuration;

import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.server.TThreadPoolServer.Args;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.transport.TTransportFactory;

import rso.at.ClientDataService;

public class ClientDataThread extends Thread{
	private TThreadPoolServer server;

	public ClientDataThread(int serverID) throws TTransportException{
		TServerSocket serverTransportExternal = new TServerSocket(Configuration.externalPort);
		TTransportFactory factory = new TFramedTransport.Factory();

		TMultiplexedProcessor processor = new TMultiplexedProcessor();
		Args args = new TThreadPoolServer.Args(serverTransportExternal);
		args.processor(processor);
		args.transportFactory(factory);

		processor.registerProcessor("ClientData", new ClientDataService.Processor<ClientDataImpl>(new ClientDataImpl()));
		server = new TThreadPoolServer(args);
	}

	public void run(){
		// Start serving
		server.serve();
	}

}
