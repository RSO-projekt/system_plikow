package impl.server.master;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

// Class representing an AT connection. 
class Connection {
	public Connection(String host, int port, int serverID, String service) {
		this.host = host;
		this.port = port;
		this.serverID = serverID;
		this.service = service;
		reopen();
	}
	
	public void reopen() {
		if (transport != null) transport.close();
		transport = new TFramedTransport(new TSocket(host, port, Configuration.sTimeout));
		try {
			transport.open();
		} catch (TTransportException e) {
			// Ignore this error
		}
		protocol = new TMultiplexedProtocol(new TBinaryProtocol(transport), service);
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
	protected TMultiplexedProtocol protocol;
	protected String host;
	protected int port;
	protected int serverID;
	String service;
}