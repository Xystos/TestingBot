package bowtie.feed.exc;

/**
 * @author &#8904
 */
public class NoValidEntriesException extends Exception
{
    private static final long serialVersionUID = 1L;

    /**
     * Create a new exception with a detail message.
     * 
     * @param message
     *            The message describing the cause of this exception.
     */
    public NoValidEntriesException(String message)
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
    public NoValidEntriesException(String message, Throwable e)
    {
        super(message, e);
    }
}