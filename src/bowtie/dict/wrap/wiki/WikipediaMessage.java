package bowtie.dict.wrap.wiki;

import java.util.List;

import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.RequestBuffer;
import bowtie.dict.util.DictionaryUtils;

/**
 * @author &#8904
 *
 */
public class WikipediaMessage
{
    private List<WikipediaWord> words;
    private int index = 0;
    private IMessage message;

    public WikipediaMessage(List<WikipediaWord> words, IMessage message)
    {
        this.words = words;
        if (this.words.size() == 0)
        {
            index = -1;
        }
        this.message = message;
    }

    public synchronized void nextPage()
    {
        if (index != -1 && index < words.size() - 1)
        {
            index ++ ;

            RequestBuffer.request(() ->
            {
                message.edit(DictionaryUtils.embedWikipediaWord(words.get(index)));
            }).get();
        }
    }

    public synchronized void previousPage()
    {
        if (index > 0)
        {
            index -- ;

            RequestBuffer.request(() ->
            {
                message.edit(DictionaryUtils.embedWikipediaWord(words.get(index)));
            }).get();
        }
    }

    public String getID()
    {
        return message.getStringID();
    }
}