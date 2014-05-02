import java.util.List;

import org.apache.thrift.TException;

import rso.at.ClientMasterService;
import rso.at.EntryNotFound;
import rso.at.FileEntry;
import rso.at.InvalidOperation;
import rso.at.Transaction;


public class ClientMasterImpl implements ClientMasterService.Iface {

	@Override
	public FileEntry getFileEntry(String path) throws EntryNotFound,
			InvalidOperation, TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<FileEntry> lookup(String path) throws EntryNotFound,
			InvalidOperation, TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<FileEntry> lookup2(FileEntry parent) throws EntryNotFound,
			InvalidOperation, TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FileEntry makeDirectory(String path) throws EntryNotFound,
			InvalidOperation, TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FileEntry makeDirectory2(FileEntry parent, String name)
			throws EntryNotFound, InvalidOperation, TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FileEntry makeFile(String path, long size) throws EntryNotFound,
			InvalidOperation, TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FileEntry makeFile2(FileEntry parent, String name, long size)
			throws EntryNotFound, InvalidOperation, TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeEntry(String path) throws EntryNotFound,
			InvalidOperation, TException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeEntry2(FileEntry entry) throws EntryNotFound,
			InvalidOperation, TException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public FileEntry moveEntry(String fromPath, String toPath)
			throws EntryNotFound, InvalidOperation, TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FileEntry moveEntry2(FileEntry entry, FileEntry parent, String name)
			throws EntryNotFound, InvalidOperation, TException {
		// TODO Auto-generated method stub
		return null;
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