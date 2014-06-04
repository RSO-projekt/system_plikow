package impl.server.data;
import org.apache.thrift.TException;

import rso.at.*;

public class MasterDataImpl implements MasterDataService.Iface{

	@Override
	public void allocateFile(long fileID, long newFileSize)
			throws InvalidOperation, TException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createFileTransaction(Transaction transaction, long fileID,
			long offset, long size) throws InvalidOperation, TException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void applyChanges(long fileID) throws InvalidOperation, TException {
		// TODO Auto-generated method stub
		
	}

}
