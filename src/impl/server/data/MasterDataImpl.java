package impl.server.data;

import org.apache.thrift.TException;

import rso.at.*;

// Tylko master
public class MasterDataImpl implements MasterDataService.Iface {

    @Override
    public void allocateFile(long fileID, long newFileSize) 
            throws InvalidOperation, TException {
        System.out.println("allocateFile fileID" + fileID + " : " + newFileSize);
        FileData.getInstance().createFile(fileID, newFileSize);
    }

    @Override
    public void createFileTransaction(Transaction transaction)
            throws InvalidOperation, TException {
        System.out.println("createFileTransaction fileID" + transaction.fileID + " : " + transaction.offset);
        FileData.getInstance().addTransaction(transaction);
    }

    @Override
    public void applyChanges(long fileID) 
            throws InvalidOperation, TException {
        System.out.println("applyChanges fileID" + fileID);
        FileData.getInstance().applyChanges(fileID);
    }

}
