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
    public void transactionFinished(Transaction transaction) throws InvalidOperation, TException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public List<FileEntryExtended> getMirroredFileList() throws InvalidOperation, TException {
        // TODO Auto-generated method stub
        return null;
    }

}
