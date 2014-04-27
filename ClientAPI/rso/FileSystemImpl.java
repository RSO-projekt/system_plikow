package rso;

import java.util.ArrayList;

import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

public class FileSystemImpl implements FileSystem {

	private TTransport transport;

	@Override
	public void connect() throws TTransportException {
		transport = new TSocket("localhost", 9090); //TODO przenieść do pliku konfiguracyjnego
		transport.open();
//		TProtocol protocol = new  TBinaryProtocol(transport);
	}

	@Override
	public void disconnect() {
		transport.close();
	}

	@Override
	public Entry getEntry(String path) throws ConnectionLostException,
			EntryNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<Entry> lookup(String path)
			throws ConnectionLostException, EntryNotFoundException,
			InvalidOperationException {
		Entry entry = getEntry(path);
		if (entry == null) //TODO Ten if docelowo powinien znaleźć się w metodzie getEntry 
			throw new EntryNotFoundException(path+" not found!");
		if (entry instanceof DirectoryEntry)
			return lookup((DirectoryEntry) entry);
		else
			throw new InvalidOperationException(entry.getName()
					+ " is not directory!");
		
	}

	@Override
	public ArrayList<Entry> lookup(DirectoryEntry dirEntry)
			throws ConnectionLostException {
		return dirEntry.getChildren();
	}

	@Override
	public DirectoryEntry makeDirectory(String path) throws ConnectionLostException,
			EntryNotFoundException, InvalidOperationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DirectoryEntry makeDirectory(DirectoryEntry parentDir, String name)
			throws ConnectionLostException, InvalidOperationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FileEntry makeFile(String path, long size)
			throws ConnectionLostException, EntryNotFoundException,
			InvalidOperationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FileEntry makeFile(DirectoryEntry parentDir, String name, long size)
			throws ConnectionLostException, InvalidOperationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeEntry(String path) throws ConnectionLostException,
			EntryNotFoundException, InvalidOperationException {
		removeEntry(getEntry(path));
	}

	@Override
	public void removeEntry(Entry entry) throws ConnectionLostException,
			InvalidOperationException {
		// TODO Auto-generated method stub

	}

	@Override
	public Entry moveEntry(String fromPath, String toPath)
			throws ConnectionLostException, EntryNotFoundException,
			InvalidOperationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Entry moveEntry(Entry entry, DirectoryEntry parentDir, String name)
			throws ConnectionLostException, InvalidOperationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void writeToFile(String filePath, long offset, byte[] bytes)
			throws ConnectionLostException, EntryNotFoundException,
			InvalidOperationException {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeToFile(FileEntry file, long offset, byte[] bytes)
			throws ConnectionLostException, InvalidOperationException {
		// TODO Auto-generated method stub

	}

	@Override
	public byte[] readFromFile(String filePath, long offset, long num)
			throws ConnectionLostException, EntryNotFoundException,
			InvalidOperationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] readFromFile(FileEntry file, long offset, long num)
			throws ConnectionLostException, InvalidOperationException {
		// TODO Auto-generated method stub
		return null;
	}

}
