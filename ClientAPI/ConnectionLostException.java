package rso;

/**
 * Exception is thrown when connection with remote file system has been reset.
 * 
 * @author Przemysław Lenart
 */
public class ConnectionLostException extends Exception {
    private static final long serialVersionUID = 5102966214217062019L;

    public ConnectionLostException(String exception) {
        super(exception);
    }
}
