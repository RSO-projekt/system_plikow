package impl.server.master;

import impl.Configuration;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

// Class representing an AT connection. 
class Connection {
    public Connection(String host, int port, int serverID) {
        this.host = host;
        this.port = port;
        this.serverID = serverID;
        reopen();
    }

    public void reopen() {
        if (transport != null)
            transport.close();
        transport = new TSocket(host, port, Configuration.sServerTimeout);
        try {
            transport.open();
        } catch (TTransportException e) {
            // Ignore this error
        }
        protocol = new TBinaryProtocol(transport);
    }

    public String getHostAddress() {
        return host;
    }

    public int getHostPort() {
        return port;
    }

    public int getServerID() {
        return serverID;
    }

    protected TTransport transport;
    protected TBinaryProtocol protocol;
    protected String host;
    protected int port;
    protected int serverID;
    String service;
}