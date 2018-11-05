package bowtie.feed.exc;

/**
 * @author &#8904
 */
public class DateUnknownException extends Exception
{
    private static final long serialVersionUID = 1L;

    /**
     * Create a new exception with a detail message.
     * 
     * @param message
     *            The message describing the cause of this exception.
     */
    public DateUnknownException(String message)
    {
        super(message);
    }

    /**
     * Create a new exception with a detail message.
     * 
     * @param message
     *            The message describing the cause of this exception.
     * @param e
     *            The exception causing this exception to be thrown.
     */
    public DateUnknownException(String message, Throwable e)
    {
        super(message, e);
    }
}