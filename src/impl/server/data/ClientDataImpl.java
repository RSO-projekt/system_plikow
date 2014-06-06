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
		return FileData.getInstance().getNextFileChunk(transaction,chunkInfo);
	}

	@Override
	public ChunkInfo sendNextFileChunk(Transaction transaction,
			FileChunk fileChunk) throws InvalidOperation, HostNotPermitted,
			TException {
		return FileData.getInstance().sendNextFileChunk(transaction,fileChunk);
	}

}
