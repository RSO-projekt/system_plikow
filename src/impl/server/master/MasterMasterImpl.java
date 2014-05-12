package impl.server.master;

import org.apache.thrift.TException;

import rso.at.*;

public class MasterMasterImpl implements MasterMasterService.Iface{
	private FileSystemMonitor monitor;
	public MasterMasterImpl(FileSystemMonitor monitor) {
		this.monitor=monitor;
	}

	@Override
	public void election(int serverID) throws TException {
		monitor.election(serverID);
	}

	@Override
	public void elected(int serverID) throws TException {
		monitor.elected(serverID);
	}

	@Override
	public void updateCreateEntry(int serverID, long fsVersion,
			FileEntryExtended entry) throws TException {
		monitor.updateCreateEntry(serverID, fsVersion, entry);
		
	}

	@Override
	public void updateRemoveEntry(int serverID, long fsVersion,
			FileEntryExtended entry) throws TException {
		monitor.updateRemoveEntry(serverID, fsVersion, entry);
	}

	@Override
	public void updateMoveEntry(int serverID, long fsVersion,
			FileEntryExtended oldEntry, FileEntryExtended newEntry)
			throws TException {
		monitor.updateMoveEntry(serverID, fsVersion, oldEntry, newEntry);
	}

	@Override
	public FileSystemSnapshot getFileSystemSnapshot(int serverID)
			throws TException {
		return monitor.getFileSystemSnapshot(serverID);
	}
}
