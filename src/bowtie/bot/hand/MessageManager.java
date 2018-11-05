package bowtie.bot.hand;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import sx.blah.discord.handle.obj.IGuild;
import bowtie.dict.wrap.urban.UrbanDictionaryMessage;
import bowtie.dict.wrap.wiki.WikipediaMessage;
import bowtie.dict.wrap.wordnet.WordnetMessage;

/**
 * @author &#8904
 *
 */
public class MessageManager
{
    private static CopyOnWriteArrayList<MessageManager> messageManagers = new CopyOnWriteArrayList<MessageManager>();
    private List<UrbanDictionaryMessage> urbanMessages;
    private List<WikipediaMessage> wikiMessages;
    private List<WordnetMessage> wordnetMessages;
    private final IGuild guild;

    public MessageManager(IGuild guild)
    {
        this.guild = guild;
        this.urbanMessages = new ArrayList<UrbanDictionaryMessage>();
        this.wikiMessages = new ArrayList<WikipediaMessage>();
        this.wordnetMessages = new ArrayList<WordnetMessage>();
    }

    public IGuild getGuild()
    {
        return this.guild;
    }

    public WordnetMessage getWordnetMessageByID(String id)
    {
        for (WordnetMessage message : wordnetMessages)
        {
            if (message.getID().equals(id))
            {
                return message;
            }
        }
        return null;
    }

    public boolean addWordnet(WordnetMessage message)
    {
        return this.wordnetMessages.add(message);
    }

    public UrbanDictionaryMessage getUrbanMessageByID(String id)
    {
        for (UrbanDictionaryMessage message : urbanMessages)
        {
            if (message.getID().equals(id))
            {
                return message;
            }
        }
        return null;
    }

    public boolean addUrban(UrbanDictionaryMessage message)
    {
        return this.urbanMessages.add(message);
    }

    public WikipediaMessage getWikiMessageByID(String id)
    {
        for (WikipediaMessage message : wikiMessages)
        {
            if (message.getID().equals(id))
            {
                return message;
            }
        }
        return null;
    }

    public boolean addWiki(WikipediaMessage message)
    {
        return this.wikiMessages.add(message);
    }

    public static void addManager(MessageManager manager)
    {
        messageManagers.add(manager);
    }

    /**
     * Returns either an existing manager or creates a new one.
     * 
     * @param guild
     * @return
     */
    public static MessageManager getManagerForGuild(IGuild guild)
    {
        for (MessageManager manager : messageManagers)
        {
            if (manager.getGuild().getStringID().equals(guild.getStringID()))
            {
                return manager;
            }
        }
        MessageManager newManager = new MessageManager(guild);
        addManager(newManager);
        return newManager;
    }
}