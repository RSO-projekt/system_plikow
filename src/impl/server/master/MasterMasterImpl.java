package impl.server.master;

import java.nio.ByteBuffer;
import java.util.List;

import org.apache.thrift.TException;

import rso.at.*;

public class MasterMasterImpl implements MasterMasterService.Iface{
	private FileSystemMonitor monitor;
	public MasterMasterImpl(FileSystemMonitor monitor) {
		this.monitor=monitor;
	}

	@Override
	public void election(int serverID) throws TException {
		// TODO Auto-generated method stub

	}

	@Override
	public void elected(int serverID) throws TException {
		// TODO Auto-generated method stub

	}

	@Override
	public void ok(int serverID) throws TException {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateCreateEntry(long fsVersion, FileEntryExtended entry)
			throws TException {
		monitor.updateCreateEntry(fsVersion, entry);
	}

	@Override
	public void updateRemoveEntry(long fsVersion, FileEntryExtended entry)
			throws TException {
		monitor.updateRemoveEntry(fsVersion, entry);
	}

	@Override
	public void updateMoveEntry(long fsVersion, FileEntryExtended oldEntry,
			FileEntryExtended newEntry) throws TException {
		monitor.updateMoveEntry(fsVersion, oldEntry, newEntry);
	}

	@Override
	public FileSystemSnapshot recreateFileSystem() throws TException {
		// TODO Funkcja zwracająca strukturę plików + numer wersji.
		return monitor.makeRecreateFileSystem();
	}
	
	//tu pewnie w interface trzeba dopisac naglowek metody zeby on tam mogl ja przeciazyc?
	public void getFileSystemFromSnapshot(FileSystemSnapshot snapshot){
		monitor.makeGetFileSystemFromSnapshot(snapshot);
	}

}
