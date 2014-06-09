package impl.server.data;
import java.nio.ByteBuffer;

import org.apache.thrift.TException;

import rso.at.DataDataService;
import rso.at.FileChunk;
import rso.at.FileEntryExtended;
import rso.at.InvalidOperation;
import rso.at.Transaction;

public class DataDataImpl implements DataDataService.Iface {

    @Override
    public void applyChanges(long fileID) throws InvalidOperation, TException {
        System.out.println("Got applyChanges for file: " + fileID);
        FileData.getInstance().applyChanges(fileID);
    }

    @Override
    public void sendFileChunk(Transaction transaction, FileChunk fileChunk)
            throws InvalidOperation, TException {
        System.out.println("Got sendFileChunk [file: " + transaction.fileID + ", num:" + fileChunk.info.number + "]");
        FileData.getInstance().sendNextFileChunk(transaction, fileChunk, false); 
    }

    @Override
    public void allocateFile(FileEntryExtended file, long newFileSize)
            throws InvalidOperation, TException {
        System.out.println("Got allocateFile [file: " + file.entry.id + ", new size: " + newFileSize + "]");
        FileData.getInstance().createFile(file, newFileSize);
    }

    @Override
    public ByteBuffer getFile(FileEntryExtended file) throws InvalidOperation,
            TException {
        System.out.println("Sending copy of file id: " + file.entry.id);
        return FileData.getInstance().getFile(file.entry.id);
    }

}
