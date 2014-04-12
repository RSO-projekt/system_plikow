package rso;

/**
 * Exception is thrown when file or directory doesn't exist.
 * 
 * @author Przemysław Lenart
 */
public class EntryNotFoundException extends Exception {
	private static final long serialVersionUID = -2362095803184946075L;

	public EntryNotFoundException(String exception) {
		super(exception);
	}
}
