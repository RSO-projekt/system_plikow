package impl.server.data;

import org.apache.thrift.TException;

import rso.at.InvalidOperation;
import rso.at.Transaction;

public interface TransactionEndListener {
    public void transactionEnded(Transaction transaction, boolean isEnded) throws InvalidOperation, TException; 
}
