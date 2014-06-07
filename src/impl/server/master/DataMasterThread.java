package impl.server.master;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.server.TThreadPoolServer.Args;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;

import rso.at.DataMasterService;

class DataMasterThread extends Thread {
    private TThreadPoolServer server;

    public DataMasterThread(TServerSocket serverTransport, FileSystemMonitor monitor, int serverID) throws TTransportException {
        DataMasterService.Processor<DataMasterImpl> processor = 
                new DataMasterService.Processor<DataMasterImpl>(new DataMasterImpl(monitor));

        TThreadPoolServer.Args args = new Args(serverTransport);
        args.processor(processor);
        args.protocolFactory(new TBinaryProtocol.Factory());
        
        server = new TThreadPoolServer(args);
        monitor.log("Starting data-master server on port " + serverTransport.getServerSocket().getLocalPort() + 
                    " with id " + serverID + "...");
    }

    public void run() {
        // Start serving
        server.serve();
    }
}