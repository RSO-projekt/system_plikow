package impl.server.data;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.thrift.TException;

import rso.at.ChunkInfo;
import rso.at.FileChunk;
import rso.at.InvalidOperation;
import rso.at.Transaction;
import rso.at.TransactionType;

public class File {
    /**
     * Move it to proper class
     * 
     */
    private final static int sTimeout = 10;

    private final static int sMaxFileChunkSize = 1000;

    private long mFileID;

    private byte[] mFileData;

    private List<FileChunk> mFileChanges;

    private long mFileSize;
    /**
     * Date - datestamp Transaction - transaction
     */
    private List<Pair<Date, Transaction>> mFileTransactions;

    private TransactionEndListener mTransactionEndListener;

    public File(long fileID,TransactionEndListener transactionEndListener) {
        this(fileID, 0,transactionEndListener);
    }

    public File(long fileID, long newFileSize, TransactionEndListener transactionEndListener) {
        mTransactionEndListener = transactionEndListener;
        mFileSize = newFileSize;
        mFileID = fileID;
        mFileTransactions = new ArrayList<Pair<Date, Transaction>>();
        mFileChanges = new ArrayList<FileChunk>();
        if (mFileSize != 0) {
            // TODO byc moze poprawic - zeby nie zutowac na int
            mFileData = new byte[(int) mFileSize];
        }
    }

    public long getFileID() {
        return mFileID;
    }

    /**
     * Deletes transactions that have their timeout breached
     */
    public void deleteOutdatedTransactions() {
        int pos = 0;
        // check if transactions have its timeout breached
        while (pos < mFileTransactions.size()) {
            if (mFileTransactions.get(pos).first.getTime() < (new Date().getTime() - sTimeout * 1000)) {
                try {
                    mTransactionEndListener.transactionEnded(mFileTransactions.get(pos).second, false);
                } catch (InvalidOperation e) {
                    
                } catch (TException e) {
                    
                }finally{
                    if(mFileTransactions.get(pos).second.type == TransactionType.WRITE){
                        rollBackChanges();
                    }
                }
                mFileTransactions.remove(pos);
            } else {
                pos++;
            }
        }
    }

    /**Deletes unstages changes bacause of error with MasterDataConnection
     * 
     */
    private void rollBackChanges() {
        mFileChanges.clear(); 
    }

    /**
     * Adds transaction to list, and checks previous transactions
     * 
     * @param transaction
     * @return
     * @throws InvalidOperation
     */
    public boolean addTransaction(Transaction transaction) throws InvalidOperation {
        deleteOutdatedTransactions();
        if (transaction.type == TransactionType.WRITE) {
            for (Pair<Date, Transaction> tmp : mFileTransactions) {
                if (tmp.second.type == TransactionType.WRITE) {
                    throw new InvalidOperation(300, "File is being modified");
                }
            }

            mFileTransactions.add(new Pair<Date, Transaction>(new Date(), transaction));
        }

        return false;
    }

    /**
     * Returns ByteBuffer with piece of file - also modifies chunk info
     * @param transaction 
     * 
     * @param chunkInfo
     * @return
     * @throws InvalidOperation 
     */
    public ByteBuffer getFileChunk(Transaction transaction, ChunkInfo chunkInfo) throws InvalidOperation {
        applyNewTimeoutToTranasaction(transaction);
        if (chunkInfo.number == -1) {
            chunkInfo.maxNumber = (int) Math.ceil((double) mFileData.length / (double) sMaxFileChunkSize);
        }
        chunkInfo.size = (chunkInfo.number * chunkInfo.size + sMaxFileChunkSize > mFileData.length ? mFileData.length - chunkInfo.number * chunkInfo.size : sMaxFileChunkSize);
        chunkInfo.number++;
        
        if(chunkInfo.number == chunkInfo.maxNumber){
            try {
                mTransactionEndListener.transactionEnded(transaction, true);
            } catch (TException e) {
                throw new InvalidOperation(310, "Could not send info about transaction completion");
            }
        }
        return ByteBuffer.wrap(mFileData, chunkInfo.number * chunkInfo.size, chunkInfo.size);
    }

    private void applyNewTimeoutToTranasaction(Transaction transaction) {
        for(Pair<Date, Transaction> tmp: mFileTransactions){
            if(tmp.second.token == transaction.token){
                tmp.first = new Date();
                break;
            }
            
        }
    }

    /**Return true if file change is finished
     * @param transaction 
     * @param fileChunk2
     * @return
     */
    public synchronized boolean addChange(Transaction transaction, FileChunk fileChunk2) {
        applyNewTimeoutToTranasaction(transaction);
        mFileChanges.add(fileChunk2);
        if(fileChunk2.info.maxNumber == fileChunk2.info.number){
            return true;
        }
        return false;
    }

    /**
     * Apply changes added by addChange function
     * 
     * @throws InvalidOperation
     */
    public synchronized void applyChanges() throws InvalidOperation {
        try {
            for (FileChunk fileChunk : mFileChanges) {
                ChunkInfo chunkInfo = fileChunk.info;
                int offset = chunkInfo.number * chunkInfo.size;
                for (int i = 0; i < chunkInfo.size; i++) {
                    mFileData[offset + i] = fileChunk.data.get(i);
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new InvalidOperation(303, "Invalid operation on file ! Change out of bounds.");
        } finally {
            mFileChanges.clear();
        }
    }

    /**Reallocates file size
     * @param newFileSize
     */
    public void rellocate(long newFileSize) {
        byte[] old_data= mFileData;
        mFileData = new byte[(int)newFileSize];
        for(int i =0; i < Math.min(mFileData.length, old_data.length); i++){
            mFileData[i] = old_data[i];
        }
        
    }
}
