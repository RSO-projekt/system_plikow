package impl.server.data;

import org.apache.thrift.TException;

import rso.at.ChunkInfo;
import rso.at.ClientDataService;
import rso.at.FileChunk;
import rso.at.HostNotPermitted;
import rso.at.InvalidOperation;
import rso.at.Transaction;

public class ClientDataImpl implements ClientDataService.Iface{

	@Override
	public FileChunk getNextFileChunk(Transaction transaction,
			ChunkInfo chunkInfo) throws InvalidOperation, HostNotPermitted,
			TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ChunkInfo sendNextFileChunk(Transaction transaction,
			ChunkInfo chunkInfo) throws InvalidOperation, HostNotPermitted,
			TException {
		// TODO Auto-generated method stub
		return null;
	}

}
