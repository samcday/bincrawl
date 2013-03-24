package au.com.samcday.bincrawl.dao.exceptions;

public class ReleaseUpdateException extends RuntimeException {
    public ReleaseUpdateException(Exception e) {
        super("Failed to create or update Release.", e);
    }
}
