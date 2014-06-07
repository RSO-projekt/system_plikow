package impl.server.data;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.server.TThreadPoolServer.Args;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;

import rso.at.MasterDataService;

public class MasterDataThread extends Thread {
    private TThreadPoolServer server;

    public MasterDataThread(TServerSocket serverTransport, int serverID) throws TTransportException {
        MasterDataService.Processor<MasterDataImpl> processor = 
                new MasterDataService.Processor<MasterDataImpl>(new MasterDataImpl());
        
        TThreadPoolServer.Args args = new Args(serverTransport);
        args.processor(processor);
        args.protocolFactory(new TBinaryProtocol.Factory());
        
        server = new TThreadPoolServer(args);
        System.out.println("Starting master-data server on port " + serverTransport.getServerSocket().getLocalPort() + 
                           " with id " + serverID + "...");
    }

    public void run() {
        server.serve();
    }

}
