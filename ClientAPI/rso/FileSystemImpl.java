package rso;

import java.util.ArrayList;

import org.apache.thrift.TException;

import rso.at.ClientMasterService;

public class FileSystemImpl implements FileSystem {

	ClientMasterService.Iface service;
	
	@Override
	public void connect() throws ConnectionLostException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void disconnect() {
		// TODO Auto-generated method stub

	}

	@Override
	public FileEntry getFileEntry(String path) throws ConnectionLostException,
			EntryNotFoundException, InvalidOperationException {
		rso.at.FileEntry entry = null;
		try {
			entry = service.getFileEntry(path);
		} catch (TException e) { //TODO do ustalenia czy korzystamy z rso.EntryNotFoundException
//										czy z rso.at.EntryNotFound czy z obu
//										moim zdaniem lepiej będzie wykorzystać te z rso.at.*
			e.printStackTrace();
		}
		return new FileEntry(entry);
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
		rso.at.FileEntry entry = null;
		try {
			entry = service.makeDirectory(path);
		} catch (TException e) { //TODO jw.
			e.printStackTrace();
		}
		return new FileEntry(entry);
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
