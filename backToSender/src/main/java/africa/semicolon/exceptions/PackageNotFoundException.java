package africa.semicolon.exceptions;

public class PackageNotFoundException extends BackToSenderException {
    public PackageNotFoundException(String message) {
        super(message);
    }
}
