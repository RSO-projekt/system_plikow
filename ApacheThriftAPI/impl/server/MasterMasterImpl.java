package impl.server;

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
	public void updateEntry(FileEntry entry, long modNumber) throws TException {
		// TODO Auto-generated method stub

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
	public List<FileEntryExtended> updateMetadata() throws TException {
		// TODO Auto-generated method stub
		return null;
	}
}
