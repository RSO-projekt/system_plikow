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

	public FileChunk getNextFileChunk(Transaction transaction, ChunkInfo chunkInfo) {
		
	}
	
}
