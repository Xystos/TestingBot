package bowtie.bot.auto;

import java.io.Serializable;
import java.util.List;

/**
 * @author &#8904
 *
 */
public class SerializableDummyMessage implements Serializable
{
    private final static long serialVersionUID = 1;
    
    /**
     * The unique snowflake ID of the object.
     */
    private final long id;

    /**
     * The raw content of the message.
     */
    private String content;

    /**
     * The author of the message.
     */
    private final long authorID;

    /**
     * The channel the message was sent in.
     */
    private final long channelID;

    /**
     * The users mentioned in the message.
     */
    private List<Long> mentions;

    /**
     * The roles mentioned in the message.
     */
    private List<Long> roleMentions;

    /**
     * Whether an @everyone mention in the message would be valid. (If the author of the message has permission to
     * mention everyone)
     */
    private boolean everyoneMentionIsValid;

    /**
     * The message's content with human-readable mentions. This is lazily evaluated.
     */
    private String formattedContent = null;

    
    public SerializableDummyMessage(long id, String content, long authorID, long channelID, List<Long> mentions, 
                                    List<Long> roleMentions, boolean mentionsEveryone)
    {
        this.id = id;
        this.content = content;
        this.authorID = authorID;
        this.channelID = channelID;
        this.mentions = mentions;
        this.roleMentions = roleMentions;
        this.everyoneMentionIsValid = mentionsEveryone;
    }
    
    public SerializableDummyMessage(DummyMessage message)
    {
        this.id = message.getLongID();
        this.content = message.getContent();
        this.authorID = message.getAuthor().getLongID();
        this.channelID = message.getChannel().getLongID();
        this.mentions = message.getLongMentions();
        this.roleMentions = message.getLongRoleMentions();
        this.everyoneMentionIsValid = message.mentionsEveryone();
    }
    
    public long getID()
    {
        return this.id;
    }
    
    public String getContent()
    {
        return this.content;
    }
    
    public long getAuthorID()
    {
        return this.authorID;
    }
    
    public long getChannelID()
    {
        return this.channelID;
    }
    
    public List<Long> getMentions()
    {
        return this.mentions;
    }
    
    public List<Long> getRoleMentions()
    {
        return this.roleMentions;
    }
    
    public boolean mentionsEveryone()
    {
        return this.everyoneMentionIsValid;
    }
}