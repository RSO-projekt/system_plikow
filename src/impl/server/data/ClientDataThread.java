package impl.server.data;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.server.TThreadPoolServer.Args;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;

import rso.at.ClientDataService;

public class ClientDataThread extends Thread {
    private TThreadPoolServer server;

    public ClientDataThread(TServerSocket serverTransport, int serverID) throws TTransportException {
        ClientDataService.Processor<ClientDataImpl> processor = 
                new ClientDataService.Processor<ClientDataImpl>(new ClientDataImpl(serverID));

        TThreadPoolServer.Args args = new Args(serverTransport);
        args.processor(processor);
        args.protocolFactory(new TBinaryProtocol.Factory());
        
        server = new TThreadPoolServer(args);
        System.out.println("Starting client-data server on port " + serverTransport.getServerSocket().getLocalPort() + 
                           " with id " + serverID + "...");
    }

    public void run() {
        server.serve();
    }

}
