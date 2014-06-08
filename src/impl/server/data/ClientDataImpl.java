package impl.server.data;

import org.apache.thrift.TException;

import rso.at.ChunkInfo;
import rso.at.ClientDataService;
import rso.at.FileChunk;
import rso.at.HostNotPermitted;
import rso.at.InvalidOperation;
import rso.at.Transaction;

public class ClientDataImpl implements ClientDataService.Iface {
    
    public ClientDataImpl(){
        FileData.getInstance().registerTransactionEndListener(new TransactionEndListener(){

            @Override
            public void transactionEnded(boolean isEnded) {
                
            }
            
        });
    }

    @Override
    public FileChunk getNextFileChunk(Transaction transaction,
            ChunkInfo chunkInfo) throws InvalidOperation, HostNotPermitted, TException {
    	System.out.println("getNectFileChunk fileID" + transaction.fileID + " : " + chunkInfo.number);
        return FileData.getInstance().getNextFileChunk(transaction, chunkInfo);
    }

    @Override
    public ChunkInfo sendNextFileChunk(Transaction transaction,
            FileChunk fileChunk) throws InvalidOperation, HostNotPermitted, TException {
        System.out.println("sendNextFileChunk fileID" + transaction.fileID + " : " + fileChunk.info.number);
        return FileData.getInstance().sendNextFileChunk(transaction, fileChunk);
    }

}
