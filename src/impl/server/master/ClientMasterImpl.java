package impl.server.master;

import impl.Configuration;
import impl.MasterDataConnection;

import java.util.List;

import org.apache.thrift.TException;
import rso.at.ClientMasterService;
import rso.at.EntryNotFound;
import rso.at.FileEntry;
import rso.at.FileEntryExtended;
import rso.at.HostNotPermitted;
import rso.at.InvalidOperation;
import rso.at.MasterDataService;
import rso.at.Transaction;
import rso.at.TransactionType;

public class ClientMasterImpl implements ClientMasterService.Iface {
    private FileSystemMonitor monitor;

    public ClientMasterImpl(FileSystemMonitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public FileEntry getFileEntry(String path) 
            throws EntryNotFound, HostNotPermitted {

        return monitor.getEntry(true, path).entry;
    }

    @Override
    public List<FileEntry> lookup(String path) 
            throws EntryNotFound, InvalidOperation, TException {

        return monitor.lookup(true, path, null);
    }

    @Override
    public List<FileEntry> lookup2(FileEntry parent) 
            throws EntryNotFound, InvalidOperation, TException {
        return monitor.lookup(true, "", parent);
    }

    @Override
    public FileEntry makeDirectory(String path) 
            throws EntryNotFound, InvalidOperation, TException {
        return monitor.makeDirectory(true, path);
    }

    @Override
    public FileEntry makeDirectory2(FileEntry parent, String name) 
            throws EntryNotFound, InvalidOperation, TException {
        return monitor.makeDirectory2(true, parent, name);
    }

    @Override
    public FileEntry makeFile(String path) 
            throws EntryNotFound, InvalidOperation, TException {
        return monitor.makeFile(true, path);
    }

    @Override
    public FileEntry makeFile2(FileEntry parent, String name) 
            throws EntryNotFound, InvalidOperation, TException {
        return monitor.makeFile2(true, parent, name);
    }

    @Override
    public void removeEntry(String path) 
            throws EntryNotFound, InvalidOperation, TException {
        monitor.removeEntry(true, path);

    }

    @Override
    public void removeEntry2(FileEntry entry) 
            throws EntryNotFound, InvalidOperation, TException {
        monitor.removeEntry2(true, entry);
    }

    @Override
    public FileEntry moveEntry(String fromPath, String toPath)
            throws EntryNotFound, InvalidOperation, TException {
        return monitor.moveEntry(true, fromPath, toPath);
    }

    @Override
    public FileEntry moveEntry2(FileEntry entry, FileEntry parent, String name) 
            throws EntryNotFound, InvalidOperation, TException {
        return monitor.moveEntry2(true, entry, parent, name);
    }

    @Override
    public Transaction writeToFile2(FileEntry file, long offset, long num) 
            throws EntryNotFound, InvalidOperation, TException {
        FileEntryExtended entryCopy = monitor.checkIfEntryIsWriteReady(file);
        MasterDataService.Iface masterDataService;
        Transaction transaction = null;
        for (Integer dataServerID : entryCopy.mirrors) {
            masterDataService = connectMasterToData(dataServerID.intValue());
            if (masterDataService != null) {
                int token = monitor.getNextTransactionToken();
                transaction = new Transaction(TransactionType.WRITE, token, dataServerID.intValue(), file.id);
                break;
            }
        }
        if (transaction == null) {
            throw new InvalidOperation(20, "Cannot connect to data server");
        }
        return transaction;
    }

    @Override
    public Transaction readFromFile(String path, long offset, long num) 
            throws EntryNotFound, InvalidOperation, TException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Transaction readFromFile2(FileEntry entry, long offset, long num) 
            throws EntryNotFound, InvalidOperation, TException {
        // TODO Auto-generated method stub
        return null;
    }

    private MasterDataService.Iface connectMasterToData(int dataServerID) 
            throws InvalidOperation {
        
        // Check if data server with specified ID exists
        if (dataServerID >= Configuration.sDataServerIPs.size()) {
            throw new InvalidOperation(500, "Wrong IP number of data server");
        }
        
        // Connect
        MasterDataConnection conn = new MasterDataConnection(dataServerID);
        if (!conn.wasCreated()) {
            return null;
        }
        
        return conn.getService();
    }

    @Override
    public FileEntry allocateFile(String path, long size) 
            throws EntryNotFound, InvalidOperation, HostNotPermitted, TException {
        FileEntry file = monitor.getEntry(true, path).entry;
        return allocateFile2(file, size);
    }

    @Override
    public FileEntry allocateFile2(FileEntry file, long size) 
            throws EntryNotFound, InvalidOperation, HostNotPermitted, TException {
        FileEntryExtended entryCopy = monitor.checkIfEntryIsAllocateReady(file);
        MasterDataService.Iface masterDataService;
        boolean modified = false;
        for (Integer dataServerID : entryCopy.mirrors) {
            masterDataService = connectMasterToData(dataServerID.intValue());
            
            if (masterDataService != null) {
                masterDataService.allocateFile(file.id, size);
                modified = true;
                break;
            }
        }
        if (!modified) {
            throw new InvalidOperation(28, "Cannot connect to data server");
        }
        monitor.setFileSize(file, size);
        return file;
    }
}