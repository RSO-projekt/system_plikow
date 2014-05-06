package rso;

import java.util.List;

import org.apache.thrift.TException;

import rso.at.ClientMasterService;
import rso.at.EntryNotFound;
import rso.at.FileEntry;
import rso.at.InvalidOperation;
import rso.at.Transaction;

public class FileSystemImpl implements FileSystem {

	ClientMasterService.Iface service;

	@Override
	public void connect() {
		// TODO Auto-generated method stub

	}

	@Override
	public void disconnect() {
		// TODO Auto-generated method stub

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
