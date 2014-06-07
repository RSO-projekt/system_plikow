package impl.server.data;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.server.TThreadPoolServer.Args;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;

import rso.at.DataDataService;

public class DataDataThread extends Thread {
    private TThreadPoolServer server;

    public DataDataThread(TServerSocket serverTransport, int serverID) throws TTransportException {
        DataDataService.Processor<DataDataImpl> processor = 
                new DataDataService.Processor<DataDataImpl>(new DataDataImpl());

        TThreadPoolServer.Args args = new Args(serverTransport);
        args.processor(processor);
        args.protocolFactory(new TBinaryProtocol.Factory());
        
        server = new TThreadPoolServer(args);
        System.out.println("Starting data-data server on port " + serverTransport.getServerSocket().getLocalPort() + 
                           " with id " + serverID + "...");
    }

    public void run() {
        server.serve();
    }

}
