package impl.client;

import impl.ClientDataConnection;
import impl.ClientMasterConnection;
import impl.Configuration;

import java.nio.ByteBuffer;
import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import rso.at.ChunkInfo;
import rso.at.ClientDataService;
import rso.at.ClientMasterService;
import rso.at.EntryNotFound;
import rso.at.FileChunk;
import rso.at.FileEntry;
import rso.at.HostNotPermitted;
import rso.at.InvalidOperation;
import rso.at.Transaction;

public class FileSystemImpl implements FileSystem {
    private ClientMasterService.Iface clientMasterService;
    private ClientDataService.Iface clientDataService;
    private ClientMasterConnection connection;

    @Override
    public void connect() throws TTransportException {
        int i = 0;
        while (clientMasterService == null && i < Configuration.sMasterServerIPs.size()) {
            System.out.print("Connecting with host: " + Configuration.sMasterServerIPs.get(i) + ":" +
                                                        Configuration.sMasterServerPorts.get(i) + "...");
            connection = new ClientMasterConnection(i);
            if (connection.wasCreated()) {
                System.out.println(" OK.");
                clientMasterService = connection.getService();
            } else {
                System.out.println(" Failed.");
                ++i;
            }
        }
        if (clientMasterService == null)
            throw new TTransportException();
    }

    @Override
    public void disconnect() {
        if (connection != null)
            connection.close();
    }

    @Override
    public FileEntry getFileEntry(String path) throws EntryNotFound,
            InvalidOperation, HostNotPermitted, TException {
        return clientMasterService.getFileEntry(path);
    }

    @Override
    public List<FileEntry> lookup(String path) throws EntryNotFound,
            InvalidOperation, HostNotPermitted, TException {
        return clientMasterService.lookup(path);
    }

    @Override
    public List<FileEntry> lookup(FileEntry dirEntry) throws EntryNotFound,
            InvalidOperation, HostNotPermitted, TException {
        return clientMasterService.lookup2(dirEntry);
    }

    @Override
    public FileEntry makeDirectory(String path) throws EntryNotFound,
            InvalidOperation, HostNotPermitted, TException {
        return clientMasterService.makeDirectory(path);
    }

    @Override
    public FileEntry makeDirectory(FileEntry parentDir, String name)
            throws EntryNotFound, HostNotPermitted, InvalidOperation,
            TException {
        return clientMasterService.makeDirectory2(parentDir, name);
    }

    @Override
    public FileEntry makeFile(String path) throws EntryNotFound,
            InvalidOperation, HostNotPermitted, TException {
        return clientMasterService.makeFile(path);
    }

    @Override
    public FileEntry makeFile(FileEntry parentDir, String name, long size)
            throws EntryNotFound, InvalidOperation, HostNotPermitted,
            TException {
        return clientMasterService.makeFile2(parentDir, name);
    }

    @Override
    public void removeEntry(String path) throws EntryNotFound,
            InvalidOperation, HostNotPermitted, TException {
        clientMasterService.removeEntry(path);
    }

    @Override
    public void removeEntry(FileEntry entry) throws EntryNotFound,
            InvalidOperation, HostNotPermitted, TException {
        clientMasterService.removeEntry2(entry);
    }

    @Override
    public FileEntry moveEntry(String fromPath, String toPath)
            throws EntryNotFound, InvalidOperation, HostNotPermitted,
            TException {
        return clientMasterService.moveEntry(fromPath, toPath);
    }

    @Override
    public FileEntry moveEntry(FileEntry entry, FileEntry parentDir, String name)
            throws EntryNotFound, InvalidOperation, HostNotPermitted,
            TException {
        return clientMasterService.moveEntry2(entry, parentDir, name);
    }

    @Override
    public void writeToFile(String filePath, long offset, byte[] bytes)
            throws EntryNotFound, InvalidOperation, HostNotPermitted,
            TException {
        FileEntry entry = clientMasterService.getFileEntry(filePath);
        Transaction transaction = clientMasterService.writeToFile2(entry, offset, bytes.length);
        sendChunks(transaction, bytes);

    }

    @Override
    public void writeToFile(FileEntry file, long offset, byte[] bytes)
            throws EntryNotFound, InvalidOperation, HostNotPermitted,
            TException {
        Transaction transaction = clientMasterService.writeToFile2(file, offset, bytes.length);
        sendChunks(transaction, bytes);

    }

    private void sendChunks(Transaction transaction, byte[] bytes)
            throws TTransportException, InvalidOperation, HostNotPermitted,
            TException {
        
        ClientDataConnection conn = new ClientDataConnection(transaction.dataServerID);
        if (!conn.wasCreated())
            throw new InvalidOperation(99, "Cannot connect to data server at: " + 
                                           conn.getHostAddress() + ":" +
                                           conn.getHostPort());
        clientDataService = conn.getService();
        
        int chunkSize = 1000;
        int maxChunkCount = (int) Math.ceil((double) bytes.length / chunkSize);
        for (int i = 0; i < maxChunkCount; i++) {
            int actualSize = Math.min(bytes.length - (i * chunkSize), chunkSize);
            ByteBuffer buffer = ByteBuffer.wrap(bytes, i * chunkSize, actualSize);
            FileChunk fileChunk = new FileChunk(buffer, new ChunkInfo(i,maxChunkCount, actualSize));
            ChunkInfo chunkInfo = clientDataService.sendNextFileChunk(transaction, fileChunk);
            System.out.println("Sent: " + (i + 1) + "/" + maxChunkCount + " packages");
            // SPRAWDZIC CZY TAM ADI DOBRZE OGARNAL NADAWANIE TEGO NUMBER
            if (chunkInfo.number != i) {
                throw new InvalidOperation(501, "Bad part sent");
            }
        }
    }

    @Override
    public byte[] readFromFile(String filePath, long offset, long num)
            throws EntryNotFound, InvalidOperation, HostNotPermitted,
            TException {
        Transaction transaction = clientMasterService.readFromFile(filePath, offset, num);
        System.out.println("YO");
        return readChunks(transaction, num);
    }

    @Override
    public byte[] readFromFile(FileEntry file, long offset, long num)
            throws EntryNotFound, InvalidOperation, HostNotPermitted,
            TException {
        Transaction transaction = clientMasterService.readFromFile2(file, offset, num);
        System.out.println("YO");
        return readChunks(transaction, num);
    }

    private byte[] readChunks(Transaction transaction, long num)
            throws InvalidOperation, HostNotPermitted, TException {
        ClientDataConnection conn = new ClientDataConnection(transaction.dataServerID);
        if (!conn.wasCreated())
            throw new InvalidOperation(99, "Cannot connect to data server at: " + 
                                           conn.getHostAddress() + ":" +
                                           conn.getHostPort());
        clientDataService = conn.getService();
        
        System.out.println("Zaalokowalem: " + num);
        ByteBuffer byteList = ByteBuffer.allocate((int) num);
        FileChunk tmpFileChunk;
        ChunkInfo chunkInfo = new ChunkInfo(0, 0, 1000);
        do {
            tmpFileChunk = clientDataService.getNextFileChunk(transaction, chunkInfo);
            System.out.println("Dostalem: " + tmpFileChunk.data.array().length);
            byteList.put(tmpFileChunk.data.array());
            chunkInfo = tmpFileChunk.info;
            System.out.println("Read " + chunkInfo.number + "/" + chunkInfo.maxNumber + " packages");
        } while (tmpFileChunk.info.number < tmpFileChunk.info.maxNumber);
        return byteList.array();
    }

    @Override
    public FileEntry allocateFile(String to, long size) throws EntryNotFound,
            InvalidOperation, HostNotPermitted, TException {

        return clientMasterService.allocateFile(to, size);
    }

}
