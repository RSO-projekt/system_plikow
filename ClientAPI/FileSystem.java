package rso;

import java.util.ArrayList;

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
	 */
	public void connect() throws ConnectionLostException;
	
	/**
	 * Cleanly disconnect from the server.
	 */
	public void disconnect() throws ConnectionLostException;
	
	/**
	 * Get information structure about a file or directory.
	 * 
	 * @param path Path to a file or directory.
	 */
	public FileEntry getFileEntry(String path) throws ConnectionLostException,
	                                                  EntryNotFoundException,
	                                                  InvalidOperationException;
	
	/**
	 * Get list of file entries in specified folder.
	 * 
	 * @param path Path to a directory in standard UNIX format: '/' is a root
	 *             directory.
	 * @return List of FileEntries in specified folder by URL.
	 */
	public ArrayList<FileEntry> lookup(String path) throws ConnectionLostException,
	                                                       EntryNotFoundException,
	                                                       InvalidOperationException;
	
	public ArrayList<FileEntry> lookup(FileEntry dirEntry) throws ConnectionLostException,
	                                                              EntryNotFoundException,
	                                                              InvalidOperationException;
	
	/**
	 * Make directory under specified path. You can create several folders in
	 * one call.
	 * 
	 * @param path Path to new directory.
	 */
	public void makeDirectory(String path) throws ConnectionLostException,
	                                              EntryNotFoundException,
	                                              InvalidOperationException;
	
	public void makeDirectory(FileEntry parentDir, String name) throws ConnectionLostException,
	                                                                   EntryNotFoundException,
	                                                                   InvalidOperationException;
	
	/**
	 * Make file under specified path. You can also specify size in bytes of a 
	 * file to be preallocated to be sure that after write there will be 
	 * sufficient space for it's content. If file already exists it behaves the
	 * same as UNIX function fallocate: extending or truncating a file.
	 */
	public void makeFile(String path, long size) throws ConnectionLostException,
	                                                    EntryNotFoundException,
	                                                    InvalidOperationException;
	
	public void makeFile(FileEntry parentDir, String name) throws ConnectionLostException,
	                                                              EntryNotFoundException,
	                                                              InvalidOperationException;
	
	/**
	 * Remove file from specified path. If it's directory, it must be empty.
	 * @param path Path to a file for removal.
	 */
	public void remove(String path) throws ConnectionLostException,
	                                       EntryNotFoundException,
	                                       InvalidOperationException;
	
	public void remove(FileEntry entry) throws ConnectionLostException,
	                                           EntryNotFoundException,
	                                           InvalidOperationException;
	
	/**
	 * Move file inside of file system. This function also can be used for
	 * file renaming.
	 * 
	 * @param fromPath Old location of a file.
	 * @param toPath New location of a file.
	 */
	public void move(String fromPath, String toPath) throws ConnectionLostException,
	                                                        EntryNotFoundException,
	                                                        InvalidOperationException;
	
	public void move(FileEntry entry, FileEntry parentDir, String name) throws ConnectionLostException,
	                                                                           EntryNotFoundException,
	                                                                           InvalidOperationException;
	
	/**
	 * Write bytes to a file at given offset. If offset + bytes.lenght is
	 * larger than size of a file, it will be extended.
	 *  
	 * @param filePath Path to a file.
	 * @param offset File offset where writing should start.
	 * @param bytes Bytes to write.
	 */
	public void write(String filePath, long offset, byte[] bytes) throws ConnectionLostException,
	                                                                     EntryNotFoundException,
	                                                                     InvalidOperationException;
	
	public void write(FileEntry file, long offset, byte[] bytes) throws ConnectionLostException,
	                                                                    EntryNotFoundException,
	                                                                    InvalidOperationException;
	
	/**
	 * Read num bytes from a file at given offset. If num + offset is bigger
	 * than file's size it will return smaller byte array than suspected.
	 * 
	 * @param filePath Path to a file.
	 * @param offset File offset where reading should start,
	 * @return Bytes read.
	 */
	public byte[] read(String filePath, long offset, long num) throws ConnectionLostException,
	                                                                  EntryNotFoundException,
	                                                                  InvalidOperationException;
	
	public byte[] read(FileEntry file, long offset, long num) throws ConnectionLostException,
	                                                                 EntryNotFoundException,
	                                                                 InvalidOperationException;
}
