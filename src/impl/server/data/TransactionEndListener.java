package impl.server.data;

import rso.at.Transaction;

public interface TransactionEndListener {
    public void transactionEnded(Transaction transaction, boolean isEnded); 
}
