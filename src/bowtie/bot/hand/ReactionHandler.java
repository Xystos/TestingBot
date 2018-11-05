package bowtie.bot.hand;

import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;
import sx.blah.discord.handle.obj.IReaction;
import sx.blah.discord.util.RequestBuffer;
import bowt.bot.Bot;
import bowtie.dict.wrap.urban.UrbanDictionaryMessage;
import bowtie.dict.wrap.wiki.WikipediaMessage;
import bowtie.dict.wrap.wordnet.WordnetMessage;

/**
 * @author &#8904
 *
 */
public class ReactionHandler implements IListener<ReactionAddEvent>
{
    private Bot bot;

    public ReactionHandler(Bot bot)
    {
        this.bot = bot;
    }

    /**
     * @see sx.blah.discord.api.events.IListener#handle(sx.blah.discord.api.events.Event)
     */
    @Override
    public void handle(ReactionAddEvent event)
    {
        IReaction reaction = event.getReaction();
        if (!event.getUser().equals(bot.getClient().getOurUser())
                && (reaction.getEmoji().getName().equals("◀")
                || reaction.getEmoji().getName().equals("▶")))
        {
            MessageManager manager = MessageManager.getManagerForGuild(event.getGuild());
            UrbanDictionaryMessage urbanMessage = manager.getUrbanMessageByID(event.getMessage().getStringID());

            if (urbanMessage != null)
            {
                if (reaction.getEmoji().getName().equals("◀"))
                {
                    urbanMessage.previousPage();
                }
                else if (reaction.getEmoji().getName().equals("▶"))
                {
                    urbanMessage.nextPage();
                }
                RequestBuffer.request(() ->
                {
                    event.getMessage().removeReaction(event.getUser(), reaction);
                }).get();
            }
            else
            {
                WikipediaMessage wikiMessage = manager.getWikiMessageByID(event.getMessage().getStringID());

                if (wikiMessage != null)
                {
                    if (reaction.getEmoji().getName().equals("◀"))
                    {
                        wikiMessage.previousPage();
                    }
                    else if (reaction.getEmoji().getName().equals("▶"))
                    {
                        wikiMessage.nextPage();
                    }
                    RequestBuffer.request(() ->
                    {
                        event.getMessage().removeReaction(event.getUser(), reaction);
                    }).get();
                }
                else
                {
                    WordnetMessage wordnetMessage = manager.getWordnetMessageByID(event.getMessage().getStringID());

                    if (wordnetMessage != null)
                    {
                        if (reaction.getEmoji().getName().equals("◀"))
                        {
                            wordnetMessage.previousPage();
                        }
                        else if (reaction.getEmoji().getName().equals("▶"))
                        {
                            wordnetMessage.nextPage();
                        }
                        RequestBuffer.request(() ->
                        {
                            event.getMessage().removeReaction(event.getUser(), reaction);
                        }).get();
                    }
                }
            }
        }
    }
}