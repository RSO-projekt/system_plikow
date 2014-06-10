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
            public void transactionEnded(Transaction transaction, boolean isEnded) {
                DataMasterConnection conn = new DataMasterConnection(transaction.masterServerID);
                if(conn.wasCreated()){
                    try {
                        conn.getService().transactionFinished(transaction, isEnded);
                    } catch (InvalidOperation e) {
                    } catch (TException e) {
                    }
                    System.out.println("Transaction finished with status: " + isEnded);
                }
            }
            
        });
    }

    @Override
    public FileChunk getNextFileChunk(Transaction transaction,
            ChunkInfo chunkInfo) throws InvalidOperation, HostNotPermitted, TException {
    	System.out.println("GetNextFileChunk [file: " + transaction.fileID + ", num: " + chunkInfo.number + "]");
        return FileData.getInstance().getNextFileChunk(transaction, chunkInfo);
    }

    @Override
    public ChunkInfo sendNextFileChunk(Transaction transaction,
            FileChunk fileChunk) throws InvalidOperation, HostNotPermitted, TException {
        System.out.println("SendNextFileChunk [file: " + transaction.fileID + ", num: " + fileChunk.info.number + "]");
        
        List<Integer> mirrors = FileData.getInstance().getFileMirrors(transaction.fileID);
        for (Integer dataID : mirrors) {
            if (dataID == myServerID) continue;
            
            System.out.println("Sending sendNextChunk [file: " + transaction.fileID + ", dataServerID: " + dataID + "]");
            DataDataConnection conn = new DataDataConnection(dataID);
            if (conn.wasCreated()) {
                conn.getService().sendFileChunk(transaction, fileChunk);
            }
        }
        ChunkInfo info = FileData.getInstance().sendNextFileChunk(transaction, fileChunk, true);
        return info;
    }

}
