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
	public FileEntry getFileEntry(String path) throws ConnectionLostException,
			EntryNotFoundException, InvalidOperationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<FileEntry> lookup(String path)
			throws ConnectionLostException, EntryNotFoundException,
			InvalidOperationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<FileEntry> lookup(FileEntry dirEntry)
			throws ConnectionLostException, EntryNotFoundException,
			InvalidOperationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FileEntry makeDirectory(String path) throws ConnectionLostException,
			EntryNotFoundException, InvalidOperationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FileEntry makeDirectory(FileEntry parentDir, String name)
			throws ConnectionLostException, EntryNotFoundException,
			InvalidOperationException {
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
	public FileEntry makeFile(FileEntry parentDir, String name, long size)
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
	public void removeEntry(FileEntry entry) throws ConnectionLostException,
			EntryNotFoundException, InvalidOperationException {
		// TODO Auto-generated method stub

	}

	@Override
	public FileEntry moveEntry(String fromPath, String toPath)
			throws ConnectionLostException, EntryNotFoundException,
			InvalidOperationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FileEntry moveEntry(FileEntry entry, FileEntry parentDir, String name)
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
	public void writeToFile(FileEntry file, long offset, byte[] bytes)
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
	public byte[] readFromFile(FileEntry file, long offset, long num)
			throws ConnectionLostException, EntryNotFoundException,
			InvalidOperationException {
		// TODO Auto-generated method stub
		return null;
	}

}
