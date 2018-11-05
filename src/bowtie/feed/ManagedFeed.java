package bowtie.feed;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;
import sx.blah.discord.util.RequestBuffer.IRequest;
import bowt.guild.GuildObject;
import bowtie.core.Main;
import bowtie.feed.exc.ChannelDeletedException;
import bowtie.feed.exc.FeedNotFoundException;
import bowtie.feed.exc.GuildNotFoundException;
import bowtie.feed.exc.NoValidEntriesException;
import bowtie.feed.util.FeedUtils;
import bowtie.feed.xml.Item;
import bowtie.json.JSONBuilder;
import bowtie.json.Jsonable;

/**
 * @author &#8904
 *
 */
public class ManagedFeed implements Jsonable
{
    private RssFeed feed;
    private IChannel channel;
    private GuildObject guild;
    private Main main;
    private String link;
    private Color color;
    private int keepCount;
    private String additionalText;
    private boolean withImage = true;
    private boolean withText = true;
    private boolean withTitle = true;
    private boolean withFooter = true;
    private boolean withFeedIcon = true;

    public ManagedFeed(RssFeed feed, Main main, GuildObject guild, IChannel channel, String link, int keepCount,
            Color color)
    {
        this.feed = feed;
        this.channel = channel;
        this.guild = guild;
        this.main = main;
        this.link = link;
        this.keepCount = keepCount;
        this.color = color;
    }

    public void update() throws ChannelDeletedException, FeedNotFoundException, GuildNotFoundException,
            NoValidEntriesException
    {
        if (this.channel != null && !this.channel.isDeleted())
        {
            if (main.getBot().getGuildObjectByID(this.guild.getStringID()) == null)
            {
                throw new GuildNotFoundException("Guild is null for " + this.link);
            }
            try
            {
                List<Item> newItems = new ArrayList<>();
                try
                {
                    newItems = this.feed.update(this.link);
                }
                catch (FeedNotFoundException | NoValidEntriesException e)
                {
                    throw e;
                }
                for (Item item : newItems)
                {
                    if (!main.getDatabase().isExistingLink(this.guild, item.getLink(), this.link, this.getChannelID()))
                    {
                        IMessage sentMessage = null;

                        if (this.additionalText == null)
                        {
                            sentMessage = this.main.getBot().sendMessage(
                                    FeedUtils.getFeedEmbed(this.feed, item, this.guild, this.color,
                                            this.withImage, this.withTitle, this.withFeedIcon,
                                            this.withFooter, this.withText), this.channel);
                        }
                        else
                        {
                            sentMessage = this.main.getBot().sendMessage(this.additionalText,
                                    FeedUtils.getFeedEmbed(this.feed, item, this.guild, this.color,
                                            this.withImage, this.withTitle, this.withFeedIcon,
                                            this.withFooter, this.withText), this.channel);
                        }

                        if (sentMessage != null)
                        {
                            String itemlink1 = "https://" + item.getLink();
                            String itemlink2 = "http://" + item.getLink();
                            if (item.getLink().startsWith("https"))
                            {
                                itemlink1 = item.getLink();
                                itemlink2 = "http" + item.getLink().substring(5);
                            }
                            else if (item.getLink().startsWith("http"))
                            {
                                itemlink1 = "https" + item.getLink().substring(4);
                                itemlink2 = item.getLink();
                            }

                            main.getDatabase().addLink(this.guild, itemlink1, this.link, this.getChannelID());
                            main.getDatabase().addLink(this.guild, itemlink2, this.link, this.getChannelID());
                            if (this.keepCount > 0)
                            {
                                main.getDatabase().addMessage(this.guild, this.link, item.getLink(),
                                        this.getChannelID(), sentMessage.getLongID());
                                List<Long> messageIDs = this.main.getDatabase().removeOldMessages(this.guild,
                                        this.link, this.keepCount);
                                List<IMessage> messages = new ArrayList<>();

                                for (long id : messageIDs)
                                {
                                    IMessage message = this.channel.fetchMessage(id);

                                    if (message != null)
                                    {
                                        messages.add(message);
                                    }
                                }
                                if (!messages.isEmpty())
                                {
                                    deleteMessages(messages);
                                }
                            }
                        }
                    }
                }
            }
            catch (MissingPermissionsException e)
            {
                throw new MissingPermissionsException("The bot has insufficient permissions for the channel '"
                        + channel.getName() + ". "
                        + "\nThe feed '" + link + "' was removed from the server.", e.getMissingPermissions());
            }
        }
        else
        {
            throw new ChannelDeletedException("Channel has been deleted on '" + guild.getGuild().getName()
                    + "'. \nThe feed '" + link + "' was removed from the server.");
        }
    }

    private void deleteMessages(List<IMessage> messages)
    {
        boolean success = false;
        int counter = 0;
        while (!success && counter < 5)
        {
            success = RequestBuffer.request(new IRequest<Boolean>()
            {
                @Override
                public Boolean request()
                {
                    try
                    {
                        channel.bulkDelete(messages);
                        return true;
                    }
                    catch (Exception e)
                    {
                        return false;
                    }
                }
            }).get();
            if (!success)
            {
                try
                {
                    Thread.sleep(5000);
                }
                catch (InterruptedException e)
                {
                }
                counter ++ ;
            }
        }
    }

    public void sendCheckMessages(IChannel channel)
    {
        List<Item> items = this.feed.getItemSet().getItemList();

        for (int i = 0; i < items.size() && i < 5; i ++ )
        {
            Item item = items.get(i);
            if (this.additionalText == null)
            {
                this.main.getBot().sendMessage(FeedUtils.getFeedEmbed(this.feed, item, this.guild, this.color,
                        this.withImage, this.withTitle, this.withFeedIcon,
                        this.withFooter, this.withText), channel);
            }
            else
            {
                this.main.getBot().sendMessage(this.additionalText,
                        FeedUtils.getFeedEmbed(this.feed, item, this.guild, this.color,
                                this.withImage, this.withTitle, this.withFeedIcon,
                                this.withFooter, this.withText), channel);
            }
        }
    }

    public boolean isImageOn()
    {
        return this.withImage;
    }

    public boolean isIconOn()
    {
        return this.withFeedIcon;
    }

    public boolean isTitleOn()
    {
        return this.withTitle;
    }

    public boolean isTextOn()
    {
        return this.withText;
    }

    public boolean isFooterOn()
    {
        return this.withFooter;
    }

    public void setImageOn(boolean on)
    {
        this.withImage = on;
    }

    public void setIconOn(boolean on)
    {
        this.withFeedIcon = on;
    }

    public void setTitleOn(boolean on)
    {
        this.withTitle = on;
    }

    public void setTextOn(boolean on)
    {
        this.withText = on;
    }

    public void setFooterOn(boolean on)
    {
        this.withFooter = on;
    }

    public void setAdditionalText(String addText)
    {
        this.additionalText = addText;
    }

    public String getAdditionalText()
    {
        return this.additionalText;
    }

    public int getKeepCount()
    {
        return this.keepCount;
    }

    public void setKeepCount(int keepCount)
    {
        this.keepCount = keepCount;
    }

    public String getXml()
    {
        return this.feed.getXml() == null ? "" : this.feed.getXml();
    }

    public String getLink()
    {
        return this.link;
    }

    public long getChannelID()
    {
        if (this.channel != null)
        {
            return this.channel.getLongID();
        }
        return -1;
    }

    public IChannel getChannel()
    {
        return this.channel;
    }

    public GuildObject getGuildObject()
    {
        return this.guild;
    }

    public Item getItem(String link)
    {
        return this.feed.getItem(link);
    }

    public Color getColor()
    {
        return this.color;
    }

    public String getTitle()
    {
        return this.feed.getTitle();
    }

    public RssFeed getFeed()
    {
        return this.feed;
    }

    public int getUpdateInterval()
    {
        return this.feed.getUpdateInterval();
    }

    public void setUpdateInterval(int interval)
    {
        this.feed.setUpdateInterval(interval);
    }

    public int getAvrgInterval()
    {
        return this.feed.getAvrgInterval();
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof ManagedFeed)
        {
            ManagedFeed feed = (ManagedFeed)o;

            if (this.getLink().equals(feed.getLink()))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * @see bowtie.json.Jsonable#toJSON()
     */
    @Override
    public JSONObject toJSON()
    {
        JSONBuilder builder = new JSONBuilder()
                .put("ChannelID", this.getChannelID())
                .put("ChannelName", (this.channel != null ? this.channel.getName() : null))
                .put("Link", this.link)
                .put("AdditionalText", this.additionalText)
                .put("KeepCount", this.keepCount)
                .put("ColorR", this.color.getRed())
                .put("ColorG", this.color.getGreen())
                .put("ColorB", this.color.getBlue())
                .put("Image", this.withImage)
                .put("Title", this.withTitle)
                .put("Icon", this.withFeedIcon)
                .put("Footer", this.withFooter)
                .put("Text", this.withText);

        return builder.toJSON();
    }
}