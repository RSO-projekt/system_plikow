package impl.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import rso.at.ClientMasterService;
import rso.at.EntryNotFound;
import rso.at.FileEntry;
import rso.at.InvalidOperation;
import rso.at.Transaction;

public class FileSystemImpl implements FileSystem {

	private static final String MASTER_SERVER_HOST = "master-server";
	private static final String MASTER_SERVER_NUM = "master-server-num";
	private static final String EXTERNAL_TIMEOUT = "external-timeout";
	private static final String EXTERNAL_PORT = "external-port";
	private final static String PATH_TO_CONFIG_FILE = "properties.conf";
	private ClientMasterService.Iface service;
	private TTransport transport;
	private Properties prop;
	private Integer port;
	private Integer timeout;

	public FileSystemImpl() throws FileNotFoundException, IOException, NumberFormatException {
		prop = new Properties();
		File configFile = new File(PATH_TO_CONFIG_FILE);
		prop.load(new FileReader(configFile));
		String portString = prop.getProperty(EXTERNAL_PORT);
		String timeoutString = prop.getProperty(EXTERNAL_TIMEOUT);
		try {
			port = new Integer(portString);
		} catch (NumberFormatException e) {
			throw new NumberFormatException("Błąd: Niepoprawny port: "+portString);
		}
		try {
			timeout = new Integer(timeoutString);
		} catch (NumberFormatException e) {
			throw new NumberFormatException("Błąd: Niepoprawny timeout: "+timeoutString);
		}
	}
	
	@Override
	public void connect() throws TTransportException {
		int masterServerNum = new Integer(prop.getProperty(MASTER_SERVER_NUM));
		int i = 0;
		while (service == null && i < masterServerNum){
			String host = prop.getProperty(MASTER_SERVER_HOST+i);
			if (host != null){
				try {
					System.out.println("Trwa łączenie z serwerem: "+host);
					createTransport(host, port, timeout);
					TProtocol protocol = new TBinaryProtocol(transport);
					TMultiplexedProtocol multiplexed = new TMultiplexedProtocol(protocol, "ClientMaster");
					service = new ClientMasterService.Client(multiplexed);
					System.out.println("Połączono.");
				} catch (TTransportException e) {
					System.out.println("Niepowodzenie.");
					++i;
				}
			}
			else{
				++i;
				System.err.println("Błąd: Master-server-host-"+i+" jest null!");
			}
		}
		if (service == null)
			throw new TTransportException();
	}
	
	private void createTransport(String host, int port, int timeout)
			throws TTransportException {
		transport = new TSocket(host, port, timeout);
		transport.open();
	}

	@Override
	public void disconnect() {
		if (transport != null && transport.isOpen())
			transport.close();
	}

	@Override
	public FileEntry getFileEntry(String path) throws EntryNotFound,
			InvalidOperation, TException {
		return service.getFileEntry(path);
	}

	@Override
	public List<FileEntry> lookup(String path) throws EntryNotFound,
			InvalidOperation, TException {
		return service.lookup(path);
	}

	@Override
	public List<FileEntry> lookup(FileEntry dirEntry) throws EntryNotFound,
			InvalidOperation, TException {
		return service.lookup2(dirEntry);
	}

	@Override
	public FileEntry makeDirectory(String path) throws EntryNotFound,
			InvalidOperation, TException {
		return service.makeDirectory(path);
	}

	@Override
	public FileEntry makeDirectory(FileEntry parentDir, String name)
			throws EntryNotFound, InvalidOperation, TException {
		return service.makeDirectory2(parentDir, name);
	}

	@Override
	public FileEntry makeFile(String path, long size) throws EntryNotFound,
			InvalidOperation, TException {
		return service.makeFile(path, size);
	}

	@Override
	public FileEntry makeFile(FileEntry parentDir, String name, long size)
			throws EntryNotFound, InvalidOperation, TException {
		return service.makeFile2(parentDir, name, size);
	}

	@Override
	public void removeEntry(String path) throws EntryNotFound,
			InvalidOperation, TException {
		service.removeEntry(path);
	}

	@Override
	public void removeEntry(FileEntry entry) throws EntryNotFound,
			InvalidOperation, TException {
		service.removeEntry2(entry);
	}

	@Override
	public FileEntry moveEntry(String fromPath, String toPath)
			throws EntryNotFound, InvalidOperation, TException {
		return service.moveEntry(fromPath, toPath);
	}

	@Override
	public FileEntry moveEntry(FileEntry entry, FileEntry parentDir, String name)
			throws EntryNotFound, InvalidOperation, TException {
		return service.moveEntry2(entry, parentDir, name);
	}

	@Override
	public void writeToFile(String filePath, long offset, byte[] bytes)
			throws EntryNotFound, InvalidOperation, TException {
		// service.writeToFile(filePath, offset, bytes); //TODO
	}

	@Override
	public void writeToFile(FileEntry file, long offset, byte[] bytes)
			throws EntryNotFound, InvalidOperation, TException {
		// service.writeToFile(file, offset, bytes); //TODO
	}

	@Override
	public byte[] readFromFile(String filePath, long offset, long num)
			throws EntryNotFound, InvalidOperation, TException {
		Transaction transaction = service.readFromFile(filePath, offset, num);
		return null; // TODO
	}

	@Override
	public byte[] readFromFile(FileEntry file, long offset, long num)
			throws EntryNotFound, InvalidOperation, TException {
		Transaction transaction = service.readFromFile2(file, offset, num);
		return null; // TODO
	}

}