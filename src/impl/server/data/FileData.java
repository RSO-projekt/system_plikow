package impl.server.data;

import java.util.ArrayList;
import java.util.List;

import rso.at.ChunkInfo;
import rso.at.FileChunk;
import rso.at.InvalidOperation;
import rso.at.Transaction;

/**Class with data files and transactions that corresponds to this data files
 * @author lagoru
 *
 */
public class FileData {
	static FileData instance;

	private List<File> mFileList;

	private FileData(){
		mFileList = new ArrayList<File>();
	}

	static public FileData getInstance(){
		if(instance == null){
			instance = new FileData();
		}
		return instance;
	} 

	/**Check if transaction is proper and do not collide with other transactions
	 * @param fileID
	 * @return
	 * @throws InvalidOperation 
	 */
	public int addTransaction(long fileID, Transaction transaction) throws InvalidOperation{
		//TODO check if ip in transaction if needed
		boolean file_found = false;
		for(File file : mFileList){
			if(file.getFileID() == fileID){
				file_found = true;
				file.addTransaction(transaction);
			}
		}
		if (!file_found) {
			throw new InvalidOperation(301, "File not found");
		}
		return 0;
	}

	public void createFile(long fileID, long newFileSize) {
		mFileList.add(new File(fileID,newFileSize));

	}

	/**Sends file chunk of adjacent transaction
	 * At start of file downloading set number in ChunkInfo with -1
	 * @param transaction
	 * @param chunkInfo
	 * @return
	 * @throws InvalidOperation 
	 */
	public FileChunk getNextFileChunk(Transaction transaction, ChunkInfo chunkInfo) throws InvalidOperation {
		File file;
		for(File f: mFileList){
			if(f.getFileID() == transaction.fileID){
				FileChunk fileChunk= new FileChunk();

				//chunkInfo is beeing modified here
				fileChunk.data = f.getFileChunk(chunkInfo);
				fileChunk.info = chunkInfo;
				return fileChunk;
			}
		}
		throw new InvalidOperation(301, "File not found");
	}

	/**Receives changes for files - filechunk is not modified here
	 * it must be modified by client
	 * @param transaction
	 * @param fileChunk2
	 * @return
	 * @throws InvalidOperation 
	 */
	public ChunkInfo sendNextFileChunk(Transaction transaction,
			FileChunk fileChunk2) throws InvalidOperation {

		for(File f: mFileList){
			if(f.getFileID() == transaction.fileID){
				ChunkInfo chunkInfo= fileChunk2.info;
				f.addChange(fileChunk2);
				return chunkInfo;
			}
		}
		throw new InvalidOperation(301, "File not found");
	}

	public void applyChanges(long fileID) throws InvalidOperation {
		for(File f: mFileList){
			if(f.getFileID() == fileID){
				f.applyChanges();
			}
		}
		throw new InvalidOperation(301, "File not found");
	}
}
