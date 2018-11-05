package bowtie.dict.wrap.urban;

import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.RequestBuffer;
import bowtie.dict.util.DictionaryUtils;

/**
 * @author &#8904
 *
 */
public class UrbanDictionaryMessage
{
    private UrbanDictionaryWord word;
    private int index = 0;
    private IMessage message;

    public UrbanDictionaryMessage(UrbanDictionaryWord word, IMessage message)
    {
        this.word = word;
        if (this.word.getDefinitionCount() == 0)
        {
            index = -1;
        }
        this.message = message;
    }

    public synchronized void nextPage()
    {
        if (index != -1 && index < word.getDefinitionCount() - 1)
        {
            index ++ ;

            RequestBuffer.request(() ->
            {
                message.edit(DictionaryUtils.embedUrbanWord(word.get(index)));
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
                message.edit(DictionaryUtils.embedUrbanWord(word.get(index)));
            }).get();
        }
    }

    public String getID()
    {
        return message.getStringID();
    }
}