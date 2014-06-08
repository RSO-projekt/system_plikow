package impl.server.master;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.server.TThreadPoolServer.Args;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;

import rso.at.MasterMasterService;

class MasterMasterThread extends Thread {
    private FileSystemMonitor monitor;
    private TThreadPoolServer server;

    public MasterMasterThread(TServerSocket serverTransport, FileSystemMonitor monitor, int serverID) throws TTransportException {
        this.monitor = monitor;
        MasterMasterService.Processor<MasterMasterImpl> processor = 
                new MasterMasterService.Processor<MasterMasterImpl>(new MasterMasterImpl(monitor));

        TThreadPoolServer.Args args = new Args(serverTransport);
        args.processor(processor);
        args.protocolFactory(new TBinaryProtocol.Factory());
        
        server = new TThreadPoolServer(args);
        monitor.log("Starting master-master server on port " + serverTransport.getServerSocket().getLocalPort() + 
                    " with id " + serverID + "...");
    }

    public void run() {
        // Start election on startup
        monitor.startElection();
        // Start serving
        server.serve();
    }
}