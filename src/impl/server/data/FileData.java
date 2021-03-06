package impl.server.data;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TException;

import rso.at.ChunkInfo;
import rso.at.FileChunk;
import rso.at.FileEntryExtended;
import rso.at.InvalidOperation;
import rso.at.Transaction;

/**
 * Class with data files and transactions that corresponds to this data files
 * 
 * @author lagoru
 * 
 */
public class FileData {
    static FileData instance;

    private List<File> mFileList;

    private TransactionEndListener mTransactionEndListener;

    private FileData() {
        mFileList = new ArrayList<File>();
    }

    static public FileData getInstance() {
        if (instance == null) {
            instance = new FileData();
        }
        return instance;
    }

    /**
     * Check if transaction is proper and do not collide with other transactions
     * 
     * @param fileID
     * @return
     * @throws InvalidOperation
     */
    public int addTransaction(Transaction transaction) throws InvalidOperation {
        // TODO check if ip in transaction if needed
        boolean file_found = false;
        for (File file : mFileList) {
            if (file.getFileID() == transaction.fileID) {
                file_found = true;
                file.addTransaction(transaction);
            }
        }
        if (!file_found) {
            throw new InvalidOperation(301, "File not found");
        }
        return 0;
    }

    public void createFile(FileEntryExtended file, long newFileSize) {
        boolean found = false;
        for(int i = 0 ; i< mFileList.size(); i++){
            if(mFileList.get(i).getFileID() == file.entry.id){
                mFileList.get(i).rellocate(newFileSize);
                found = true;
            }
        }
        if(!found){
            mFileList.add(new File(file, newFileSize, mTransactionEndListener));
        }
    }

    /**
     * Sends file chunk of adjacent transaction At start of file downloading set
     * number in ChunkInfo with -1
     * 
     * @param transaction
     * @param chunkInfo
     * @return
     * @throws InvalidOperation
     */
    public FileChunk getNextFileChunk(Transaction transaction, ChunkInfo chunkInfo) throws InvalidOperation {
        for (File f : mFileList) {
            if (f.getFileID() == transaction.fileID) {
                FileChunk fileChunk = new FileChunk();

                // chunkInfo is beeing modified here
                fileChunk.data = f.getFileChunk(transaction,chunkInfo);
                fileChunk.info = chunkInfo;
                return fileChunk;
            }
        }
        throw new InvalidOperation(301, "File not found");
    }

    public void applyChanges(long fileID) throws InvalidOperation {
        for (File f : mFileList) {
            if (f.getFileID() == fileID) {
                f.applyChanges();
                return;
            }
        }
        throw new InvalidOperation(301, "File not found");
    }

    public void registerTransactionEndListener(
            TransactionEndListener transactionEndListener) {
        mTransactionEndListener = transactionEndListener;
        
    }

    /**
     * Receives changes for files - filechunk is not modified here it must be
     * modified by client
     * 
     * @param transaction
     * @param fileChunk2
     * @param checkIfTransactionExists 
     * @return
     * @throws InvalidOperation
     */
    public ChunkInfo sendNextFileChunk(Transaction transaction, FileChunk fileChunk2, boolean checkIfTransactionExists) throws InvalidOperation {

        for (File f : mFileList) {
            if (f.getFileID() == transaction.fileID) {
                ChunkInfo chunkInfo = fileChunk2.info;
                
                if(f.addChange(transaction,fileChunk2,checkIfTransactionExists)){
                    try {
                        mTransactionEndListener.transactionEnded(transaction,true);
                    } catch (TException e) {
                        e.printStackTrace();
                        throw new InvalidOperation(310, "Could not send info about transaction completion");
                    }
                }
                
                return chunkInfo;
            }
        }
        throw new InvalidOperation(301, "File not found");
    }

    public List<Integer> getFileMirrors(long fileID) throws InvalidOperation {
        for (File f : mFileList) {
            if (f.getFileID() == fileID) {
                return f.getMirrors();
            }
        }
        throw new InvalidOperation(302, "File not found");
    }

    public ByteBuffer getFile(long id) throws InvalidOperation {
        for (File f : mFileList) {
            if (f.getFileID() == id) {
                return f.getFile();
            }
        }
        throw new InvalidOperation(303, "File not found");
    }

    public void createFile(FileEntryExtended file, byte[] array) {
        File tmp = new File(file, file.entry.size, mTransactionEndListener);
        mFileList.add(tmp);
        tmp.setData(array);
    }
}
