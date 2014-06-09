package impl.server.data;

import java.util.List;

import impl.DataDataConnection;

import org.apache.thrift.TException;

import rso.at.*;

// Tylko master
public class MasterDataImpl implements MasterDataService.Iface {

    int myServerID;
    public MasterDataImpl(int serverID) {
        myServerID = serverID;
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
        
        List<Integer> mirrors = FileData.getInstance().getFileMirrors(fileID);
        for (Integer dataID : mirrors) {
            if (dataID == myServerID) continue;
            
            System.out.println("Sending applyChanges(" + fileID + ") to data server " + dataID);
            DataDataConnection conn = new DataDataConnection(dataID);
            if (conn.wasCreated()) {
                conn.getService().applyChanges(fileID);
            }
        }
        
    }

    @Override
    public void allocateFile(FileEntryExtended file, long newFileSize)
            throws InvalidOperation, TException {
        System.out.println("allocateFile fileID: " + file.entry.id + ", size: " + newFileSize);
        FileData.getInstance().createFile(file, newFileSize);
        
        for (Integer dataID : file.mirrors) {
            if (dataID == myServerID) continue;
            
            System.out.println("Sending allocateFile(" + file.entry.id + ") to data server " + dataID);
            DataDataConnection conn = new DataDataConnection(dataID);
            if (conn.wasCreated()) {
                conn.getService().allocateFile(file, newFileSize);
            }
        }
    }

}
