package impl;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

// Class representing an AT connection. 
public class Connection {
    public Connection(String host, int port, int serverID, boolean isServerConnection) {
        this.host = host;
        this.port = port;
        this.serverID = serverID;
        this.isServerConnection = isServerConnection;
        reopen();
    }

    public void reopen() {
        if (transport != null)
            transport.close();
        transport = new TSocket(host, port, isServerConnection ? Configuration.sServerTimeout : Configuration.sClientTimeout);
        created = true;
        try {
            transport.open();
        } catch (TTransportException e) {
            // Ignore this error
            created = false;
        }
        protocol = new TBinaryProtocol(transport);
    }

    public void close() {
        transport.close();
    }
    
    public String getHostAddress() {
        return host;
    }
    
    public boolean wasCreated() {
        return created;
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
    protected boolean created;
    protected boolean isServerConnection;
    String service;
}