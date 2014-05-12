package impl.client;

import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import rso.at.EntryNotFound;
import rso.at.FileEntry;
import rso.at.HostNotPermitted;
import rso.at.InvalidOperation;

/**
 * Class interface for a communication to Distributed File System service. All
 * functions declared below should be implemented by using Apache Thrift
 * library.
 * 
 * @author Przemysław Lenart
 */
public interface FileSystem {
    /**
     * Initial function which establish connection between client and a main
     * server.
     * @throws TTransportException 
     */
    public void connect() throws TTransportException;
    
    /**
     * Cleanly disconnect from the server.
     */
    public void disconnect();
    
    /**
     * Get information structure about a file or directory.
     * 
     * @param path Path to a file or directory.
     * @throws TException 
     * @throws InvalidOperation 
     * @throws EntryNotFound 
     */
    public FileEntry getFileEntry(String path) throws EntryNotFound,
                                                      InvalidOperation,
                                                      HostNotPermitted,
                                                      TException;
    
    /**
     * Get list of file entries in specified folder.
     * 
     * @param path Path to a directory in standard UNIX format: '/' is a root
     *             directory.
     * @return List of FileEntries in specified folder by URL.
     */
    public List<FileEntry> lookup(String path) throws EntryNotFound,
                                                      InvalidOperation,
                                                      HostNotPermitted,
                                                      TException;
    
    public List<FileEntry> lookup(FileEntry dirEntry) throws EntryNotFound,
                                                             InvalidOperation,
                                                             HostNotPermitted,
                                                             TException;
    
    /**
     * Make directory under specified path. You can create several folders in
     * one call.
     * 
     * @param path Path to new directory.
     * @return New directory's entry.
     */
    public FileEntry makeDirectory(String path) throws EntryNotFound,
                                                       InvalidOperation,
                                                       HostNotPermitted,
                                                       TException;
    
    public FileEntry makeDirectory(FileEntry parentDir, String name) throws EntryNotFound,
                                                                            InvalidOperation,
                                                                            HostNotPermitted,
                                                                            TException;
    
    /**
     * Make file under specified path. You can also specify size in bytes of a 
     * file to be preallocated to be sure that after write there will be 
     * sufficient space for it's content. If file already exists it behaves the
     * same as UNIX function fallocate: extending or truncating a file.
     *
     * @return New file's entry.
     */
    public FileEntry makeFile(String path, long size) throws EntryNotFound,
                                                             InvalidOperation,
                                                             HostNotPermitted,
                                                             TException;
    
    public FileEntry makeFile(FileEntry parentDir, String name, long size) throws EntryNotFound,
                                                                                  InvalidOperation,
                                                                                  HostNotPermitted,
                                                                                  TException;
    
    /**
     * Remove file from specified path. If it's directory, it must be empty.
     * @param path Path to a file for removal.
     */
    public void removeEntry(String path) throws EntryNotFound,
                                                InvalidOperation,
                                                HostNotPermitted,
                                                TException;
    
    public void removeEntry(FileEntry entry) throws EntryNotFound,
                                                    InvalidOperation,
                                                    HostNotPermitted,
                                                    TException;
    
    /**
     * Move file inside of file system. This function also can be used for
     * file renaming.
     * 
     * @param fromPath Old location of a file.
     * @param toPath New location of a file.
     *
     * @return Modified entry.
     */
    public FileEntry moveEntry(String fromPath, String toPath) throws EntryNotFound,
                                                                      InvalidOperation,
                                                                      HostNotPermitted,
                                                                      TException;
    
    public FileEntry moveEntry(FileEntry entry, FileEntry parentDir, String name) throws EntryNotFound,
                                                                                         InvalidOperation,
                                                                                         HostNotPermitted,
                                                                                         TException;
    
    /**
     * Write bytes to a file at given offset. If offset + bytes.lenght is
     * larger than size of a file, it will be extended.
     *  
     * @param filePath Path to a file.
     * @param offset File offset where writing should start.
     * @param bytes Bytes to write.
     */
    public void writeToFile(String filePath, long offset, byte[] bytes) throws EntryNotFound,
                                                                               InvalidOperation,
                                                                               HostNotPermitted,
                                                                               TException;
    
    public void writeToFile(FileEntry file, long offset, byte[] bytes) throws EntryNotFound,
                                                                              InvalidOperation,
                                                                              HostNotPermitted,
                                                                              TException;
    
    /**
     * Read num bytes from a file at given offset. If num + offset is bigger
     * than file's size it will return smaller byte array than suspected.
     * 
     * @param filePath Path to a file.
     * @param offset File offset where reading should start,
     * @return Bytes read.
     */
    public byte[] readFromFile(String filePath, long offset, long num) throws EntryNotFound,
                                                                              InvalidOperation,
                                                                              HostNotPermitted,
                                                                              TException;
    
    public byte[] readFromFile(FileEntry file, long offset, long num) throws EntryNotFound,
                                                                             InvalidOperation,
                                                                             HostNotPermitted,
                                                                             TException;
}
