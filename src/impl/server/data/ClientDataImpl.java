package impl.server.data;

import java.util.List;

import org.apache.thrift.TException;

import impl.*;
import rso.at.ChunkInfo;
import rso.at.ClientDataService;
import rso.at.FileChunk;
import rso.at.HostNotPermitted;
import rso.at.InvalidOperation;
import rso.at.Transaction;

public class ClientDataImpl implements ClientDataService.Iface {
    int myServerID;
    
    ClientDataImpl(int serverID) {
        myServerID = serverID;
        FileData.getInstance().registerTransactionEndListener(new TransactionEndListener(){

            @Override
            public void transactionEnded(Transaction transaction, boolean isEnded) throws InvalidOperation, TException {
                DataMasterConnection conn = new DataMasterConnection(transaction.masterServerID);
                if(conn.wasCreated()){
                    conn.getService().transactionFinished(transaction, isEnded);
                }
            }
            
        });
    }

    @Override
    public FileChunk getNextFileChunk(Transaction transaction,
            ChunkInfo chunkInfo) throws InvalidOperation, HostNotPermitted, TException {
    	System.out.println("getNextFileChunk fileID" + transaction.fileID + " : " + chunkInfo.number);
        return FileData.getInstance().getNextFileChunk(transaction, chunkInfo);
    }

    @Override
    public ChunkInfo sendNextFileChunk(Transaction transaction,
            FileChunk fileChunk) throws InvalidOperation, HostNotPermitted, TException {
        System.out.println("sendNextFileChunk fileID" + transaction.fileID + " : " + fileChunk.info.number);
        
        List<Integer> mirrors = FileData.getInstance().getFileMirrors(transaction.fileID);
        for (Integer dataID : mirrors) {
            if (dataID == myServerID) continue;
            
            System.out.println("Sending sendNextChunk(" + transaction.fileID + ") to data server " + dataID);
            DataDataConnection conn = new DataDataConnection(dataID);
            if (conn.wasCreated()) {
                conn.getService().sendFileChunk(transaction, fileChunk);
            }
        }
        ChunkInfo info = FileData.getInstance().sendNextFileChunk(transaction, fileChunk, true);
        return info;
    }

}
