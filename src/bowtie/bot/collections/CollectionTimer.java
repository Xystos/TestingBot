package bowtie.bot.collections;

/**
 * @author &#8904
 *
 */
public class CollectionTimer
{
    private final String name;
    private final long time;
    
    public CollectionTimer(String name, long time)
    {
        this.name = name;
        this.time = time;
    }
    
    public long getTime()
    {
        return this.time;
    }
    
    public String getName()
    {
        return this.name;
    }
}