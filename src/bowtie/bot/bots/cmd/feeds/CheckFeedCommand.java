package bowtie.bot.bots.cmd.feeds;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.util.EmbedBuilder;
import bowt.bot.Bot;
import bowt.cmnd.Command;
import bowt.cmnd.CommandCooldown;
import bowt.cons.Colors;
import bowt.evnt.impl.CommandEvent;
import bowt.guild.GuildObject;
import bowt.util.perm.UserPermissions;
import bowtie.core.Main;
import bowtie.feed.FeedManager;
import bowtie.feed.ManagedFeed;
import bowtie.feed.RssFeed;
import bowtie.feed.exc.FeedNotFoundException;
import bowtie.feed.exc.NoValidEntriesException;
import bowtie.feed.util.FeedUtils;
import bowtie.feed.xml.Item;

/**
 * @author &#8904
 *
 */
public class CheckFeedCommand extends Command
{
    private Bot bot;
    private Main main;

    /**
     * @param validExpressions
     * @param permission
     */
    public CheckFeedCommand(String[] validExpressions, int permission, Bot bot, Main main)
    {
        super(validExpressions, permission, true);
        this.bot = bot;
        this.main = main;
    }

    /**
     * @param validExpressions
     * @param permission
     */
    public CheckFeedCommand(List<String> validExpressions, int permission, Bot bot, Main main)
    {
        super(validExpressions, permission, true);
        this.bot = bot;
        this.main = main;
    }

    /**
     * @see bowtie.bot.obj.Command#execute(bowtie.evnt.impl.CommandEvent)
     */
    @Override
    public void execute(CommandEvent event)
    {
        String content = event.getMessage().getContent();
        content = content.trim().replaceAll(" +", " ");
        String[] parts = content.trim().split(" ");

        if (parts.length < 2)
        {
            this.bot.sendMessage("You have to add the URL of the feed.",
                    event.getMessage().getChannel(), Colors.RED);
            return;
        }

        String link = parts[1].trim();

        if (!link.startsWith("https://"))
        {
            if (link.startsWith("http://") && link.length() >= 8)
            {
                try
                {
                    new URL(link);
                }
                catch (MalformedURLException e)
                {
                    link = link.substring(7);
                    link = "https://" + link;
                }
            }
            else if (link.startsWith("https://") && link.length() >= 9)
            {
                try
                {
                    new URL(link);
                }
                catch (MalformedURLException e)
                {
                    link = link.substring(8);
                    link = "http://" + link;
                }
            }
            else
            {
                try
                {
                    new URL("http://" + link);
                    link = "http://" + link;
                }
                catch (MalformedURLException e)
                {
                    link = "https://" + link;
                }
            }
        }

        if (!bot.isCreator(event.getAuthor()))
        {
            new CommandCooldown(this, 60000, event.getGuildObject()).startTimer();
            ;
        }

        ManagedFeed managedFeed = FeedManager.getFeed(link, event.getGuildObject().getStringID());

        if (managedFeed != null)
        {
            managedFeed.sendCheckMessages(event.getChannel());
        }
        else
        {
            RssFeed feed = null;
            try
            {
                feed = FeedUtils.getFeedFromUrl(link);
                feed.setup();
            }
            catch (FeedNotFoundException e)
            {
                this.bot.sendMessage("Could not create a feed from the given URL.",
                        event.getMessage().getChannel(), Colors.RED);
                return;
            }
            catch (NoValidEntriesException e)
            {
                this.bot.sendMessage("The feed does not contain any valid entries.",
                        event.getMessage().getChannel(), Colors.RED);
                return;
            }
            List<Item> items = feed.getItemSet().getItemList();
            for (int i = 0; i < items.size() && i < 5; i ++ )
            {
                Item item = items.get(i);
                this.main.getBot().sendMessage(
                        FeedUtils.getFeedEmbed(feed, item, event.getGuildObject(), Colors.PURPLE), event.getChannel());
            }
        }
    }

    /**
     * @see bowtie.bot.obj.Command#getHelp()
     */
    @Override
    public EmbedObject getHelp(GuildObject guild)
    {
        EmbedBuilder builder = new EmbedBuilder();

        builder.withAuthorName("Check RSS Feed Command");
        builder.withAuthorUrl("http://www.bowtiebots.xyz/_commands_.html#rss");
        builder.withAuthorIcon(Main.BOWTIE_BOT_ICON);

        builder.withDescription("|  This command will send up to 5 articles of "
                + "|  the specified feed. This is only meant to be used to view \n"
                + "|  changes of the settings. The articles might not be the \n"
                + "|  newest ones and/or in order.\n"
                + "|");

        builder.appendField(
                "|  Usage:",
                "|  " + guild.getPrefix() + "check <feed url>\n"
                        + "|  " + guild.getPrefix() + "check https://www.reddit.com/r/discordapp/new/.rss \n"
                        + "|", false);

        builder.appendField(
                "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
                        + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~",
                "[This command needs " + UserPermissions.getPermissionString(this.getPermissionOverride(guild))
                        + " permissions](http://www.bowtiebots.xyz/master-system.html)", false);
        builder.withFooterText("Click the title to read about this command on the website.");
        return builder.build();
    }
}