package africa.semicolon.exceptions;

public class BackToSenderException extends RuntimeException {
    public BackToSenderException(String message) {
        super(message);
    }
}
