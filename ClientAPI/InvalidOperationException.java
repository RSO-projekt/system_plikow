package rso;

public class InvalidOperationException extends Exception {
    private static final long serialVersionUID = -9054617706404751039L;

    public InvalidOperationException(String exception) {
        super(exception);
    }
}
