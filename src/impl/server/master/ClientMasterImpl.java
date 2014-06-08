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
        monitor.log("Writing to file");
        FileEntryExtended entryCopy = monitor.checkIfEntryIsWriteReadReady(file);
        if (offset + num > entryCopy.entry.size) {
            throw new InvalidOperation(88, "Write operation not inside a file");
        }
        MasterDataService.Iface masterDataService;
        Transaction transaction = null;
        monitor.log("Writing to file2");
        for (Integer dataServerID : entryCopy.mirrors) {
            monitor.log("Yeah...");
            masterDataService = connectMasterToData(dataServerID.intValue());
            if (masterDataService != null) {
                transaction = monitor.getNewTransaction(file, dataServerID.intValue(), 
                                                        TransactionType.WRITE, offset, num);
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
        FileEntry entry = monitor.getEntry(true, path).entry;
        return readFromFile2(entry, offset, num);
    }

    @Override
    public Transaction readFromFile2(FileEntry entry, long offset, long num) 
            throws EntryNotFound, InvalidOperation, TException {
        FileEntryExtended entryCopy = monitor.checkIfEntryIsWriteReadReady(entry);
        if (offset + num > entryCopy.entry.size) {
            throw new InvalidOperation(88, "Read operation not inside a file");
        }
        MasterDataService.Iface masterDataService;
        Transaction transaction = null;
        for (Integer dataServerID : entryCopy.mirrors) {
            masterDataService = connectMasterToData(dataServerID.intValue());
            if (masterDataService != null) {
                transaction = monitor.getNewTransaction(entry, dataServerID.intValue(), 
                                                        TransactionType.READ, offset, num);
                break;
            }
        }
        if (transaction == null) {
            throw new InvalidOperation(20, "Cannot connect to data server");
        }
        return transaction;
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
        System.out.println("Aloc");
        FileEntry file = monitor.getEntry(true, path).entry;
        return allocateFile2(file, size);
    }

    @Override
    public FileEntry allocateFile2(FileEntry file, long size) 
            throws EntryNotFound, InvalidOperation, HostNotPermitted, TException {
        System.out.println("Aloc2");
        FileEntryExtended entryCopy = monitor.checkIfEntryIsAllocateReady(file);
        MasterDataService.Iface masterDataService;
        boolean modified = false;
        System.out.println("Aloc3");
        for (Integer dataServerID : entryCopy.mirrors) {
            System.out.println("Aloc4");
            masterDataService = connectMasterToData(dataServerID.intValue());
            System.out.println("Aloc5");
            
            if (masterDataService != null) {
                // TODO: wyscig
                System.out.println("OK");
                masterDataService.allocateFile(file.id, size);
                System.out.println("OK2");
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