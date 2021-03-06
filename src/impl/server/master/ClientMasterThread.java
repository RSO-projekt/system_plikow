package impl.server.master;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.server.TThreadPoolServer.Args;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;

import rso.at.ClientMasterService;

public class ClientMasterThread extends Thread {
    TServer server;

    public ClientMasterThread(TServerSocket serverTransport, FileSystemMonitor monitor, int serverID) throws TTransportException {
        ClientMasterService.Processor<ClientMasterImpl> processor = 
                new ClientMasterService.Processor<ClientMasterImpl>(new ClientMasterImpl(monitor));

        TThreadPoolServer.Args args = new Args(serverTransport);
        args.processor(processor);
        args.protocolFactory(new TBinaryProtocol.Factory());
        
        server = new TThreadPoolServer(args);
        monitor.log("Starting client-master server on port " + serverTransport.getServerSocket().getLocalPort() + 
                    " with id " + serverID + "...");
    }

    public void run() {
        server.serve();
    }

}
