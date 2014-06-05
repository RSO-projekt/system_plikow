package impl.server.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import rso.at.InvalidOperation;
import rso.at.Transaction;
import rso.at.TransactionType;

public class File {
	
	/**Move it to proper class
	 * 
	 */
	private final int mTimeout = 10;
	
	private long mFileID;
	
	private byte[] mFileData;
	
	private long mFileSize;
	/**
	 * Date - datestamp
	 * Transaction - transaction
	 */
	private List<Pair< Date,Transaction> > mFileTransactions;
	
	public File(long fileID){
		this(fileID, 0);
	}

	public File(long fileID, long newFileSize) {
		mFileSize = newFileSize;
		mFileID = fileID;
		mFileTransactions = new ArrayList<Pair< Date,Transaction> >();
		if(mFileSize != 0){
			//TODO byc moze poprawic - zeby nie zutowac na int
			mFileData = new byte[(int)mFileSize];
		}
	}

	public long getFileID(){
		return mFileID;
	}

	
	/**
	 * Deletes transactions that have their timeout breached
	 */
	public void deleteOutdatedTransactions(){
		int pos = 0;
		//check if transactions have its timeout breached
		while(pos < mFileTransactions.size()){
			if(mFileTransactions.get(pos).first.getTime() < (new Date().getTime() - mTimeout*1000)){
				mFileTransactions.remove(pos);
			}else{
				pos++;
			}
		}
	}
	
	/**Adds transaction to list, and checks previous transactions
	 * @param transaction
	 * @return
	 * @throws InvalidOperation 
	 */
	public boolean addTransaction(Transaction transaction) throws InvalidOperation {
		deleteOutdatedTransactions();
		if(transaction.type == TransactionType.READ){
			for(Pair<Date,Transaction> tmp : mFileTransactions){
				if(tmp.second.type == TransactionType.WRITE){
					throw new InvalidOperation(300, "File is being modified");
				}
			}
			//TODO check if this is current date
			mFileTransactions.add(new Pair<Date, Transaction>(new Date(), transaction));
		}else if(transaction.type == TransactionType.WRITE){
			for(Pair<Date,Transaction> tmp : mFileTransactions){
				if(tmp.second.type == TransactionType.WRITE){
					throw new InvalidOperation(300, "File is being modified");
				}else if(tmp.second.type == TransactionType.READ){
					throw new InvalidOperation(302, "File is being read");
				}
			}
			
			mFileTransactions.add(new Pair<Date, Transaction>(new Date(), transaction));
		}
		
		return false;
	}
}
