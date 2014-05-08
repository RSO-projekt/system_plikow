package impl.server;
import java.util.List;

import org.apache.thrift.TException;

import rso.at.ClientMasterService;
import rso.at.EntryNotFound;
import rso.at.FileEntry;
import rso.at.FileEntryExtended;
import rso.at.FileType;
import rso.at.InvalidOperation;
import rso.at.Transaction;


public class ClientMasterImpl implements ClientMasterService.Iface {
	private FileSystemMonitor monitor;
	public ClientMasterImpl(FileSystemMonitor monitor) {
		this.monitor=monitor;
	}

	@Override
	public FileEntry getFileEntry(String path) throws EntryNotFound,
			InvalidOperation, TException {
	
		return monitor.getEntry(path).entry;
	}

	@Override
	public List<FileEntry> lookup(String path) throws EntryNotFound,
			InvalidOperation, TException {
		
		return monitor.lookup(path, null);
	}

	@Override
	public List<FileEntry> lookup2(FileEntry parent) throws EntryNotFound,
			InvalidOperation, TException {
		return monitor.lookup("", parent);
	}

	@Override
	public FileEntry makeDirectory(String path) throws EntryNotFound,
			InvalidOperation, TException {
		return monitor.makeDirectory(path);
	}

	@Override
	public FileEntry makeDirectory2(FileEntry parent, String name)
			throws EntryNotFound, InvalidOperation, TException {
		return monitor.makeDirectory2(parent, name);
	}

	@Override
	public FileEntry makeFile(String path, long size) throws EntryNotFound,
			InvalidOperation, TException {
		return monitor.makeFile(path, size);
	}

	@Override
	public FileEntry makeFile2(FileEntry parent, String name, long size)
			throws EntryNotFound, InvalidOperation, TException {
		return monitor.makeFile2(parent, name, size);
	}

	@Override
	public void removeEntry(String path) throws EntryNotFound,
			InvalidOperation, TException {
		monitor.removeEntry(path);
		
	}

	@Override
	public void removeEntry2(FileEntry entry) throws EntryNotFound,
			InvalidOperation, TException {
		monitor.removeEntry2(entry);
	}

	@Override
	public FileEntry moveEntry(String fromPath, String toPath)
			throws EntryNotFound, InvalidOperation, TException {
		return monitor.moveEntry(fromPath, toPath);
	}

	@Override
	public FileEntry moveEntry2(FileEntry entry, FileEntry parent, String name)
			throws EntryNotFound, InvalidOperation, TException {
		return monitor.moveEntry2(entry, parent, name);
	}

	@Override
	public Transaction writeToFile(String path, long offset, long num)
			throws EntryNotFound, InvalidOperation, TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Transaction writeToFile2(FileEntry file, long offset, long num)
			throws EntryNotFound, InvalidOperation, TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Transaction readFromFile(String path, long offset, long num)
			throws EntryNotFound, InvalidOperation, TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Transaction readFromFile2(FileEntry entry, long offset, long num)
			throws EntryNotFound, InvalidOperation, TException {
		// TODO Auto-generated method stub
		return null;
	}
}