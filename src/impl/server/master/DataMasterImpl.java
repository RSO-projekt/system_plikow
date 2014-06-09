package impl.server.master;

import java.util.List;

import org.apache.thrift.TException;

import rso.at.DataMasterService.Iface;
import rso.at.FileEntryExtended;
import rso.at.InvalidOperation;
import rso.at.Transaction;

public class DataMasterImpl implements Iface {
    private FileSystemMonitor monitor;
    
    public DataMasterImpl(FileSystemMonitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public void transactionFinished(Transaction transaction,
            boolean isSuccessful) throws InvalidOperation, TException {
        monitor.removeFinishedTransaction(transaction, isSuccessful);
    }

    @Override
    public List<FileEntryExtended> getMirroredFileList(int serverDataID)
            throws InvalidOperation, TException {
        return monitor.getMirroredFileList(serverDataID);
    }

}
