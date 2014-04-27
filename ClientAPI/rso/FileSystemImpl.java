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
	public Entry getFileEntry(String path) throws ConnectionLostException,
			EntryNotFoundException, InvalidOperationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<Entry> lookup(String path)
			throws ConnectionLostException, EntryNotFoundException,
			InvalidOperationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<Entry> lookup(Entry dirEntry)
			throws ConnectionLostException, EntryNotFoundException,
			InvalidOperationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Entry makeDirectory(String path) throws ConnectionLostException,
			EntryNotFoundException, InvalidOperationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Entry makeDirectory(Entry parentDir, String name)
			throws ConnectionLostException, EntryNotFoundException,
			InvalidOperationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Entry makeFile(String path, long size)
			throws ConnectionLostException, EntryNotFoundException,
			InvalidOperationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Entry makeFile(Entry parentDir, String name, long size)
			throws ConnectionLostException, EntryNotFoundException,
			InvalidOperationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeEntry(String path) throws ConnectionLostException,
			EntryNotFoundException, InvalidOperationException {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeEntry(Entry entry) throws ConnectionLostException,
			EntryNotFoundException, InvalidOperationException {
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
	public Entry moveEntry(Entry entry, Entry parentDir, String name)
			throws ConnectionLostException, EntryNotFoundException,
			InvalidOperationException {
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
	public void writeToFile(Entry file, long offset, byte[] bytes)
			throws ConnectionLostException, EntryNotFoundException,
			InvalidOperationException {
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
	public byte[] readFromFile(Entry file, long offset, long num)
			throws ConnectionLostException, EntryNotFoundException,
			InvalidOperationException {
		// TODO Auto-generated method stub
		return null;
	}

}
